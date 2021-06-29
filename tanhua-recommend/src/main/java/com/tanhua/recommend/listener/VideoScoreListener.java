package com.tanhua.recommend.listener;

import com.alibaba.fastjson.JSON;
import com.tanhua.domain.mongo.VideoScore;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RocketMQMessageListener(
        topic = "tanhua-video",consumerGroup = "tanhua-video-group"
)
public class VideoScoreListener implements RocketMQListener<String> {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 1、转化map集合
     * 2、获取参数（userId,publishId,pid，type）
     * 3、构造对象 publishScore
     * 4、根据不同的type，设置不同的评分
     * 5、保存到mongodb中
     */
    public void onMessage(String message) {
        try {
            //1、转化map集合
            Map<String,String> msg = JSON.parseObject(message,Map.class);
            //2、获取参数（userId,publishId,pid，type）
            Long userId = Long.valueOf(msg.get("userId"));
            String videoId = msg.get("videoId");
            Long vid = Long.valueOf(msg.get("vid"));
            Integer type = Integer.valueOf(msg.get("type"));

            //3、构造对象 publishScore
            VideoScore ps = new VideoScore();
            ps.setUserId(userId);
            ps.setVideoId(vid);
            ps.setId(ObjectId.get());
            ps.setDate(System.currentTimeMillis());
            //4、根据不同的type，设置不同的评分
            switch (type) {
                case 1: { //发布视频
                    ps.setScore(2d);
                    break;
                }
                case 2: { //视频点赞
                    ps.setScore(5d);
                    break;
                }
                case 3: { //取消点赞
                    ps.setScore(-5d);
                    break;
                }
                case 4: { //发布评论
                    ps.setScore(10d);
                    break;
                }
                default: {
                    ps.setScore(0d);
                    break;
                }
            }

            //5、保存到mongodb中
            mongoTemplate.save(ps);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
