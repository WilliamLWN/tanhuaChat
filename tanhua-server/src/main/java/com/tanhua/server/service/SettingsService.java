package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.Settings;
import com.tanhua.domain.db.User;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.SettingsVo;
import com.tanhua.dubbo.api.BlackListApi;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.SettingsApi;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class SettingsService {

    @Reference
    private SettingsApi settingsApi;
    @Reference
    private QuestionApi questionApi;
    @Reference
    private BlackListApi blackListApi;
    @Reference
    private UserApi userApi;
    @Autowired
    private RedisTemplate redisTemplate;
    // 定义redis中存储的验证码的key的前缀
    private final String SMS_KEY = "SMS_KEY_";

    /**
     * 通知设置读取：
     * @return  id、strangerQuestion、phone、likeNotification、pinglunNotification、gonggaoNotification
     */
    public ResponseEntity<Object> querySettings() {
        //1. 获取当前用户
        User user = UserHolder.getUser();
        //2. 构造返回值vo对象，封装返回结果
        SettingsVo settingsVo = new SettingsVo();

        //3. 查询陌生人问题，并设置到vo中
        Question question = questionApi.findByUserId(user.getId());
        if (question != null) {
            settingsVo.setStrangerQuestion(question.getTxt());
        }

        //4. 设置手机号到vo中
        settingsVo.setPhone(user.getMobile());

        //5. 查询通知设置，并设置到vo中
        Settings settings = settingsApi.findByUserId(user.getId());
        if (settings != null) {
            BeanUtils.copyProperties(settings,settingsVo);
        }

        System.out.println("settingsVo.getLikeNotification() = " + settingsVo.getLikeNotification());
        System.out.println("settingsVo.getPinglunNotification() = " + settingsVo.getPinglunNotification());
        System.out.println("settingsVo.getGonggaoNotification() = " + settingsVo.getGonggaoNotification());

        return ResponseEntity.ok(settingsVo);
    }

    /**
     * 接口名称：通知设置 - 保存
     */
    public ResponseEntity<Object> saveNotification(Settings param) {
        //1. 从当前线程上获取用户信息
        Long userId = UserHolder.getUserId();

        //2. 根据用户id查询通知设置
        Settings settings = settingsApi.findByUserId(userId);

        //3. 判断
        if (settings == null) {
            //3.1 没有查到通知设置，执行保存
            settings = new Settings();
            BeanUtils.copyProperties(param,settings);
            settings.setUserId(userId);
            //如果查找为null就插入
            settingsApi.save(settings);
        } else {
            //3.2 查询到通知设置，执行修改
            settings.setPinglunNotification(param.getPinglunNotification());
            settings.setGonggaoNotification(param.getGonggaoNotification());
            settings.setLikeNotification(param.getLikeNotification());
            //如果查找不为null就修改数据
            settingsApi.update(settings);
        }
        return ResponseEntity.ok(null);
    }

    /**
     * 接口名称：设置陌生人问题 - 保存
     */
    public ResponseEntity<Object> saveQuestion(String content) {
        //1. 从当前线程上获取用户信息
        Long userId = UserHolder.getUserId();
        //2.根据用户id，查询陌生人问题
        Question question = questionApi.findByUserId(userId);
        //3. 判断
        if (question == null) {
            //3.1 如果陌生人问题为空，则添加
            question = new Question();
            question.setUserId(userId);
            question.setTxt(content);
            questionApi.save(question);
        } else {
            //3.2 如果陌生人问题不为空，则修改
            question.setTxt(content);
            questionApi.update(question);
        }
        return ResponseEntity.ok(null);
    }

    /**
     * 接口名称：黑名单 - 翻页列表
     */
    public ResponseEntity<Object> blacklist(Integer page, Integer pagesize) {
        //1. 获取用户id
        Long userId = UserHolder.getUserId();
        //2. 调用Api服务分页查询
        IPage<UserInfo> iPage = blackListApi.findBlackList(page, pagesize, userId);
        //3. 封装返回结果：PageResult
        PageResult pageResult = new PageResult(page,pagesize, (int) iPage.getTotal(),iPage.getRecords());
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 接口名称：黑名单 - 移除
     */
    public ResponseEntity<Object> deleteBlackUser(Long blackUserId) {
        // 获取登陆用户id
        Long userId = UserHolder.getUserId();
        // 调用api服务，移除黑名单用户(从tb_black_list表中删除一条记录)
        blackListApi.deleteBlackUser(userId,blackUserId);
        return ResponseEntity.ok(null);
    }

    /**
     * 接口名称：修改手机号 - 发送验证码
     * @return
     */
    public ResponseEntity<Object> sendVerificationCode() {
        //1. 生成6位随机数
        //String code = (int)((Math.random() * 9 + 1) * 100000) + "";
        String code = "123456";
        //2. 发送短信验证码
//        smsTemplate.sendSms(phone,code);

        User user = UserHolder.getUser();
        String phone = user.getMobile();
        System.out.println("phone = " + phone);
        //3. 验证码存储到redis中，key是: SMS_KEY_ + 手机号; 有效时间5分钟
        redisTemplate.opsForValue().set(SMS_KEY+phone,code, Duration.ofMinutes(5));
        return ResponseEntity.ok(null);
    }

    /**
     * 接口名称：修改手机号 - 验证码校验
     * @return verification boolean
     */
    public ResponseEntity<Object> checkVerificationCode(String verificationCode) {
        User user = UserHolder.getUser();
        String phone = user.getMobile();
        //1. 从redis中获取验证码并校验验证码
        String redisCode = (String) redisTemplate.opsForValue().get(SMS_KEY + phone);

        Map<String,Boolean> map = new HashMap<>();

        if (redisCode == null || !redisCode.equals(verificationCode)){
            return ResponseEntity.status(500).body(ErrorResult.loginError());
        }

        //2. 校验完毕，从redis中删除验证码
        redisTemplate.delete(SMS_KEY+phone);

        map.put("verification", true);
        System.out.println("map = " + map);
        return ResponseEntity.ok(map);
    }

    /**
     * 接口名称：修改手机号 - 保存
     * @param phone
     * @return null
     */
    public ResponseEntity<Object> saveNewPhone(String phone) {
        User user = UserHolder.getUser();
        user.setMobile(phone);
        userApi.update(user);
        return ResponseEntity.ok(null);
    }
}
