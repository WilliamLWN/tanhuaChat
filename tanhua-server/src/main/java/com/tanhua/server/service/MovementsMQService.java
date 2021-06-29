package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.domain.db.User;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.dubbo.api.mongo.PublishApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MovementsMQService {

    @Autowired
    private RocketMQTemplate template;

    @Reference
    private PublishApi publishApi;

    /**
     * 发布动态消息
     */
    public void publishMsg(String publishId) {
        this.sendMsg(publishId, 1);
    }

    /**
     * 浏览动态消息
     */
    public void queryPublishMsg(String publishId) {
        this.sendMsg(publishId, 2);
    }

    /**
     * 点赞动态消息
     */
    public void likePublishMsg(String publishId) {
        this.sendMsg(publishId, 3);
    }

    /**
     * 取消点赞动态消息
     */
    public void disLikePublishMsg(String publishId) {
        this.sendMsg(publishId, 6);
    }

    /**
     * 喜欢动态消息
     */
    public void lovePublishMsg(String publishId) {
        this.sendMsg(publishId, 4);
    }

    /**
     * 取消喜欢动态消息
     */
    public void disLovePublishMsg(String publishId) {
        this.sendMsg(publishId, 7);
    }

    /**
     * 评论动态消息
     */
    public void commentPublishMsg(String publishId) {
        this.sendMsg(publishId, 5);
    }

    /**
     * 发送圈子操作相关的消息
     * 	type
     *     1-发动态，2-浏览动态， 3-点赞， 4-喜欢， 5-评论，6-取消点赞，7-取消喜欢
     */
    private void sendMsg(String publishId, Integer type) {
        try {
            User user = UserHolder.getUser();
            Publish publish = this.publishApi.findById(publishId);
            //构建消息
            Map<String, String> msg = new HashMap<>();
            msg.put("userId", user.getId().toString());
            msg.put("publishId", publishId);
            msg.put("pid", publish.getPid().toString());
            msg.put("type", type.toString());
            this.template.convertAndSend("tanhua-quanzi", JSON.toJSONString(msg));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("发送消息失败! publishId = " + publishId + ", type = " + type);
        }
    }
}
