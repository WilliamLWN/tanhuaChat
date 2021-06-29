package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.domain.mongo.Video;
import com.tanhua.dubbo.api.mongo.VideoApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 对动态的操作，发送mq消息到中间件
 */
@Service
public class VideoMqService {

    @Reference
    private VideoApi videoApi;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发布视频
     */
    public void videoMsg(String videoId) {
        this.sendMsg(videoId, 1);
    }


    /**
     * 对视频点赞
     */
    public void likeVideoMsg(String videoId) {
        this.sendMsg(videoId, 2);
    }

    /**
     * 取消视频点赞
     */
    public void disLikeVideoMsg(String videoId) {
        this.sendMsg(videoId, 3);
    }

    /**
     * 评论视频
     */
    public void commentVideoMsg(String videoId) {
        this.sendMsg(videoId, 4);
    }



    /**
     * 发送消息
     *  参数：动态id
     *  参数type：
     *     type 1-发动态，2-点赞， 3-取消点赞，4-评论
     */
    private void sendMsg(String videoId, Integer type) {
        try {
            Map<String,String> message = new HashMap<>();
            message.put("userId", UserHolder.getUserId().toString());
            message.put("videoId", videoId);
            message.put("type", type.toString());
            Video video = videoApi.findById(videoId);
            message.put("vid", video.getVid().toString());
            rocketMQTemplate.convertAndSend("tanhua-video", JSON.toJSONString(message));
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("发送消息失败! publishId = " + videoId + ", type = " + type);
        }
    }
}
