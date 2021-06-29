package com.tanhua.manage.listener;

import com.tanhua.commons.templates.HuaWeiUGCTemplate;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.dubbo.api.mongo.PublishApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic = "tanhua-publish",
        consumerGroup = "tanhua-publish-consumer")
@Slf4j
public class PublishMessageListener implements RocketMQListener<String> {

    @Reference
    private PublishApi publishApi;
    @Autowired
    private HuaWeiUGCTemplate huaWeiUGCTemplate;

    @Override
    public void onMessage(String publishId) {
        log.info("消息监听器，动态内容审核，动态ID：" + publishId);
        Publish publish = publishApi.findById(publishId);
        if (publish != null && publish.getState() == 0) {
            boolean contentCheck = huaWeiUGCTemplate.textContentCheck(publish.getTextContent());
            if (contentCheck) {
                // 内容审核通过，图片审核
                boolean imageCheck = huaWeiUGCTemplate.imageContentCheck(publish.getMedias().toArray(new String[]{}));
                // 审核驳回
                Integer state = 2;
                if (imageCheck) {
                    // 审核成功
                    state = 1;
                }
                publishApi.updateState(publishId,state);
            }
        }
    }
}
