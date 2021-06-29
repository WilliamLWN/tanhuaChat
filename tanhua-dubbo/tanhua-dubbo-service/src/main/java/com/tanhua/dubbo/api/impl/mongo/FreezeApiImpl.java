package com.tanhua.dubbo.api.impl.mongo;

import com.tanhua.domain.mongo.FreezeUser;
import com.tanhua.dubbo.api.mongo.FreezeApi;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@Service
public class FreezeApiImpl implements FreezeApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void save(FreezeUser freezeUser) {
        mongoTemplate.save(freezeUser);
    }

    @Override
    public List<FreezeUser> find() {
        Query query = new Query(new Criteria("state").is(1));
        return mongoTemplate.find(query, FreezeUser.class);
    }

    @Override
    public void unfreeze(FreezeUser freezeUser) {
        Query query = new Query(new Criteria("userId").is(freezeUser.getUserId()).and("created").is(freezeUser.getCreated()));
        Update update = new Update();
        update.set("state", freezeUser.getState());
        update.set("reasonsForThawing", freezeUser.getReasonsForThawing());
        mongoTemplate.updateFirst(query, update, FreezeUser.class);
    }

    @Override
    public FreezeUser findByUserId(Integer userId) {
        Query query = new Query(new Criteria("userId").is(userId).and("state").is(1));
        return mongoTemplate.findOne(query, FreezeUser.class);
    }
}
