package com.tanhua.dubbo.api.impl.mongo;

import com.tanhua.domain.mongo.Voice;
import com.tanhua.dubbo.api.mongo.VoiceApi;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VoiceApiImpl implements VoiceApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void save(Voice voice) {
        mongoTemplate.save(voice);
    }

    @Override
    public Map<String, Object> findVoice(Long userId) {
        Query query = new Query(new Criteria("state").is(0).and("userId").ne(userId));
        List<Voice> voiceList = mongoTemplate.find(query, Voice.class);
        long count = mongoTemplate.count(query, Voice.class);
        if(count==0) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("voiceList", voiceList);
        map.put("count", count);
        return map;
    }

    @Override
    public void updateById(Voice voice) {
        Query query = new Query(new Criteria("id").is(voice.getId()));
        Update update = new Update();
        update.set("state", 1);
        mongoTemplate.updateFirst(query, update, Voice.class);
    }
}
