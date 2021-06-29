package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.utils.StringUtils;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.User;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.mongo.Voice;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.domain.vo.VoiceVo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.VoiceApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class VoiceService {

    @Autowired
    private OssTemplate ossTemplate;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Reference
    private VoiceApi voiceApi;
    @Reference
    private UserInfoApi userInfoApi;
    private String voiceKey = "VOICE_KEY_";

    /**
     * 桃花传音-接收语音
     *
     * @return
     */
    public ResponseEntity<Object> peachblossom() {

        Integer remainingTimes = 9;
        String remainingTimesStr;
        Long userId = UserHolder.getUserId();

        //先查语音数据
        Map<String, Object> voiceMap = voiceApi.findVoice(userId);
        if(voiceMap == null) {
            return ResponseEntity.status(500).body(ErrorResult.voiceError());
        }

        //如果今天第一次接收语音，就redis设置一个键值对
        if (!redisTemplate.hasKey(voiceKey + userId)) {
            remainingTimesStr = JSON.toJSONString(remainingTimes);
            redisTemplate.opsForValue().set(voiceKey + userId, remainingTimesStr, Duration.ofSeconds(getSecondNumber()));
            List<Voice> voiceList = (List<Voice>) voiceMap.get("voiceList");
            long count = (long) voiceMap.get("count");
            Random random = new Random();
            Voice voice = voiceList.get(random.nextInt((int) count));  //索引从0开始
            voiceApi.updateById(voice);  //修改state为1，设置本条语音已被获取过

            UserInfo userInfo = userInfoApi.findById(voice.getUserId());
            VoiceVo voiceVo = new VoiceVo();
            voiceVo.setId(voice.getUserId());
            voiceVo.setAvatar(userInfo.getAvatar());
            voiceVo.setNickname(userInfo.getNickname());
            voiceVo.setGender(voice.getGender());
            voiceVo.setAge(voice.getAge());
            voiceVo.setSoundUrl(voice.getSoundUrl());
            voiceVo.setRemainingTimes(remainingTimes);

            return ResponseEntity.ok(voiceVo);
        }


        //如果不是今天的第一次接收语音，就判断今天还有没有接收次数
        String remainingTimesString = redisTemplate.opsForValue().get(voiceKey + userId);
        remainingTimesStr = String.valueOf(JSON.parse(remainingTimesString));
        remainingTimes = Integer.valueOf(remainingTimesStr);
        //次数减一
        remainingTimes -= 1;
        if (remainingTimesStr!=null && Integer.valueOf(remainingTimesStr) == 0) {
            return ResponseEntity.status(500).body(ErrorResult.remainingTimesError());
        }

        List<Voice> voiceList = (List<Voice>) voiceMap.get("voiceList");
        long count = (long) voiceMap.get("count");
        Random random = new Random();
        Voice voice = voiceList.get(random.nextInt((int) count));  //索引从0开始
        voiceApi.updateById(voice);  //修改state为1，设置本条语音已被获取过

        UserInfo userInfo = userInfoApi.findById(voice.getUserId());
        VoiceVo voiceVo = new VoiceVo();
        voiceVo.setId(voice.getUserId());
        voiceVo.setAvatar(userInfo.getAvatar());
        voiceVo.setNickname(userInfo.getNickname());
        voiceVo.setGender(voice.getGender());
        voiceVo.setAge(voice.getAge());
        voiceVo.setSoundUrl(voice.getSoundUrl());
        voiceVo.setRemainingTimes(remainingTimes);


        remainingTimesStr = JSON.toJSONString(remainingTimes);
        //删除旧的
        redisTemplate.delete(voiceKey + userId);
        //新增新的
        redisTemplate.opsForValue().set(voiceKey + userId, remainingTimesStr, Duration.ofSeconds(getSecondNumber()));

        return ResponseEntity.ok(voiceVo);
    }

    /**
     * 桃花传音-发送语音
     *
     * @return
     */
    public ResponseEntity<Object> sendpeachblossom(MultipartFile soundFile) throws IOException {
        Long userId = UserHolder.getUserId();
        UserInfo userInfo = userInfoApi.findById(userId);

        //2.音频上传到阿里云
        String voiceFileName = soundFile.getOriginalFilename();
//        System.out.println("voiceFileName = " + voiceFileName);
        String voiceUrl = ossTemplate.uploadVoice(voiceFileName, soundFile.getInputStream());
//        System.out.println("voiceUrl = " + voiceUrl);

        //3. 保存video
        Voice voice = new Voice();
        //[设置主键id]
        voice.setId(ObjectId.get());
        voice.setCreated(System.currentTimeMillis());
        voice.setUserId(userId);
        voice.setSoundUrl(voiceUrl);
        voice.setGender(userInfo.getGender());
        voice.setAge(userInfo.getAge());
        voice.setState(0);  //0为未被获取过

        voiceApi.save(voice);
//        System.out.println("voice = " + voice);
        // [发送mq消息]
//        videoMqService.videoMsg(video.getId().toString());
        return ResponseEntity.ok(null);
    }

    /**
     * 今天到明天0时到秒差
     *
     * @return
     */

    //获取当前时间，到凌晨 相差的秒数
    private long getSecondNumber() {
        LocalTime midnight = LocalTime.MIDNIGHT;
        LocalDate today = LocalDate.now();
        LocalDateTime todayMidnight = LocalDateTime.of( today, midnight );
        LocalDateTime tomorrowMidnight = todayMidnight.plusDays( 1 );
        return TimeUnit.NANOSECONDS.toSeconds( Duration.between( LocalDateTime.now(), tomorrowMidnight ).toNanos() );
    }
}
