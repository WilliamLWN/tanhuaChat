package com.tanhua.dubbo.api.impl.mongo;

import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.mongo.FriendApi;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Service
public class FriendApiImpl implements FriendApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void save(Long userId, Long friendId) {
        /****** userId的好友是 friendId *********/
        // 添加好友关系到数据库中。好友关系是相互的，比如1的好友是2，同时2的好友也是1
        Query query = new Query(Criteria.where("userId").is(userId).and("friendId").is(friendId));
        // 如果不存在好友关系则添加
        if (!mongoTemplate.exists(query, Friend.class)) {
            Friend friend = new Friend();
            friend.setId(ObjectId.get());
            friend.setUserId(userId);
            friend.setFriendId(friendId);
            friend.setCreated(System.currentTimeMillis());
            // 保存好友
            mongoTemplate.save(friend);
        }
        /****** friendId的好友是 userId *********/
        query = new Query(Criteria.where("friendId").is(userId).and("userId").is(friendId));
        if (!mongoTemplate.exists(query, Friend.class)) {
            Friend friend = new Friend();
            friend.setId(ObjectId.get());
            friend.setUserId(friendId);
            friend.setFriendId(userId);
            friend.setCreated(System.currentTimeMillis());
            // 保存好友
            mongoTemplate.save(friend);
        }
    }

    @Override
    public PageResult findFriendByUserId(Integer page, Integer pagesize, String keyword, Long userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        query.limit(pagesize).skip((page-1)*pagesize);
        List<Friend> friendList = mongoTemplate.find(query, Friend.class);
        long count = mongoTemplate.count(query, Friend.class);
        return new PageResult(page,pagesize, (int) count,friendList);
    }

    @Override
    public void delete(Long unlikeUserId, Long userId) {
        Query query = new Query(
                Criteria.where("userId").is(userId)
                        .and("friendId").is(unlikeUserId)
        );
        mongoTemplate.remove(query, Friend.class);
    }
}
