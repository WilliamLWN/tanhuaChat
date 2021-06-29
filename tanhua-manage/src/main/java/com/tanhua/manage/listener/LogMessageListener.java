package com.tanhua.manage.listener;

import com.alibaba.fastjson.JSON;
import com.tanhua.manage.domain.Log;
import com.tanhua.manage.service.LogService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
@RocketMQMessageListener(topic = "tanhua-log",consumerGroup = "tanhua-log-consumer")
public class LogMessageListener implements RocketMQListener<String> {

    @Autowired
    private LogService logService;

    @Override
    public void onMessage(String message) {
        //1. 获取消息数据
        Map<String,String> map = JSON.parseObject(message, Map.class);
        Long userId = Long.valueOf(map.get("userId"+""));
        String type = map.get("type");
        String date = map.get("date");
        //2. 创建日志对象,封装消息数据
        Log log = new Log();
        log.setUserId(userId.longValue());
        log.setLogTime(date);
        log.setType(type);
        log.setCreated(new Date());
        log.setUpdated(new Date());
        //3. 保存日志
        logService.save(log);
    }
}
