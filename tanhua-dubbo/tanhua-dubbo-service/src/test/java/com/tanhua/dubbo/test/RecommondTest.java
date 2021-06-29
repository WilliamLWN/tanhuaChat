package com.tanhua.dubbo.test;

import com.tanhua.domain.db.User;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.mongo.PublishScore;
import com.tanhua.dubbo.DubboServerApplication;
import com.tanhua.dubbo.mapper.UserMapper;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DubboServerApplication.class)
public class RecommondTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void testPublishScoreData() {

        List<User> users = userMapper.selectList(null);

        List<Publish> list = mongoTemplate.find(new Query(), Publish.class);

        for (int i = 0; i < 1000; i++) {
            PublishScore score = new PublishScore();
            score.setId(ObjectId.get());
            score.setDate(System.currentTimeMillis());
            Publish publish = list.get(new Random().nextInt(list.size()));
            score.setPublishId(publish.getPid());
            score.setScore(Double.valueOf(new Random().nextInt(10)));
            User user = users.get(new Random().nextInt(5));
            score.setUserId(user.getId());
            mongoTemplate.save(score);
        }
    }
}
