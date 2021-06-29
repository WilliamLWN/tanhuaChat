package com.tanhua.dubbo.api.impl.mongo;

import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.mongo.UserLike;
import com.tanhua.domain.mongo.Visitors;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.mongo.UserLikeApi;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserLikeApiImpl implements UserLikeApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Long queryEachLoveCount(Long userId) {
        // db.tanhua_users.count({userId:20})
        Query query = new Query(Criteria.where("userId").is(userId));
        return mongoTemplate.count(query, Friend.class);
    }

    @Override
    public Long queryLoveCount(Long userId) {
        // db.user_like.count({userId:20})
        Query query = new Query(Criteria.where("userId").is(userId));
        return mongoTemplate.count(query, UserLike.class);
    }

    @Override
    public Long queryFanCount(Long userId) {
        // db.user_like.count({likeUserId:20})
        Query query = new Query(Criteria.where("likeUserId").is(userId));
        return mongoTemplate.count(query, UserLike.class);
    }

    //互相喜欢:   db.tanhua_users.find({userId:20})
    @Override
    public PageResult queryEachLoveList(Long userId, Integer page, Integer pagesize) {
        Query query = new Query(Criteria.where("userId").is(userId));
        query.limit(pagesize).skip((page - 1) * pagesize);
        List<Friend> friendList = mongoTemplate.find(query, Friend.class);
        long count = mongoTemplate.count(query, Friend.class);

        // 统一返回一个map，存储喜欢的用户id、缘分值。再通过List封装map
        List<Map<String, Object>> result = new ArrayList<>();
        // 封装返回结果
        if (friendList != null && friendList.size() > 0) {
            for (Friend friend : friendList) {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", friend.getFriendId());
                map.put("score", queryScore(userId, friend.getFriendId()));
                result.add(map);
            }
        }
        // 返回分页对象
        return new PageResult(page, pagesize, (int) count, result);
    }

    //喜欢:      db.user_like.find({userId:20})
    @Override
    public PageResult queryLoveList(Long userId, Integer page, Integer pagesize) {
        Query query = new Query(Criteria.where("userId").is(userId));
        query.limit(pagesize).skip((page - 1) * pagesize);
        List<UserLike> userLikeList = mongoTemplate.find(query, UserLike.class);
        long count = mongoTemplate.count(query, UserLike.class);

        // 统一返回一个map，存储喜欢的用户id、缘分值。再通过List封装map
        List<Map<String, Object>> result = new ArrayList<>();
        // 封装返回结果
        if (userLikeList != null && userLikeList.size() > 0) {
            for (UserLike userLike : userLikeList) {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", userLike.getLikeUserId());
                map.put("score", queryScore(userId, userLike.getLikeUserId()));
                result.add(map);
            }
        }
        // 返回分页对象
        return new PageResult(page, pagesize, (int) count, result);
    }

    //粉丝：     db.user_like.find({likeUserId:20})
    @Override
    public PageResult queryFanList(Long userId, Integer page, Integer pagesize) {
        Query query = new Query(Criteria.where("likeUserId").is(userId));
        query.limit(pagesize).skip((page - 1) * pagesize);
        List<UserLike> userLikeList = mongoTemplate.find(query, UserLike.class);
        long count = mongoTemplate.count(query, UserLike.class);

        // 统一返回一个map，存储喜欢的用户id、缘分值。再通过List封装map
        List<Map<String, Object>> result = new ArrayList<>();
        // 封装返回结果
        if (userLikeList != null && userLikeList.size() > 0) {
            for (UserLike userLike : userLikeList) {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", userLike.getUserId());
                map.put("score", queryScore(userLike.getLikeUserId(), userId));
                result.add(map);
            }
        }
        // 返回分页对象
        return new PageResult(page, pagesize, (int) count, result);
    }

    //谁看过我 :  db.visitors.find({userId:20})
    @Override
    public PageResult queryVisitorList(Long userId, Integer page, Integer pagesize) {
        Query query = new Query(Criteria.where("userId").is(userId));
        query.limit(pagesize).skip((page - 1) * pagesize);
        List<Visitors> visitorsList = mongoTemplate.find(query, Visitors.class);
        long count = mongoTemplate.count(query, Visitors.class);

        // 统一返回一个map，存储喜欢的用户id、缘分值。再通过List封装map
        List<Map<String, Object>> result = new ArrayList<>();
        // 封装返回结果
        if (visitorsList != null && visitorsList.size() > 0) {
            for (Visitors visitors : visitorsList) {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", visitors.getVisitorUserId());
                map.put("score", queryScore(userId, visitors.getVisitorUserId()));
                result.add(map);
            }
        }
        // 返回分页对象
        return new PageResult(page, pagesize, (int) count, result);
    }

    @Override
    public void delete(Long likeUserId, Long userId) {
        Query query = new Query(
                Criteria.where("userId").is(likeUserId)
                        .and("likeUserId").is(userId)
        );
        mongoTemplate.remove(query, UserLike.class);
    }

    @Override
    public void deleteLike(Long userId, Long likeUserId) {
        Query query = new Query(
                Criteria.where("userId").is(userId)
                        .and("likeUserId").is(likeUserId)
        );
        mongoTemplate.remove(query, UserLike.class);
    }

    // 保存粉丝
    @Override
    public void save(Long userId, Long likeUserId) {
        UserLike userLike = new UserLike();
        userLike.setId(ObjectId.get());
        userLike.setUserId(likeUserId);
        userLike.setLikeUserId(userId);
        userLike.setCreated(System.currentTimeMillis());
        // 保存粉丝
        mongoTemplate.save(userLike);
    }

    // 保存喜欢
    @Override
    public void saveLike(Long userId, Long likeUserId) {
        UserLike userLike = new UserLike();
        userLike.setId(ObjectId.get());
        userLike.setUserId(userId);
        userLike.setLikeUserId(likeUserId);
        userLike.setCreated(System.currentTimeMillis());
        // 保存喜欢
        mongoTemplate.save(userLike);
    }

    @Override
    public UserLike findMyLike(Long userId, Long likeUserId) {
        Query query = new Query(new Criteria("userId").is(userId).and("likeUserId").is(likeUserId));
        UserLike userLike = mongoTemplate.findOne(query, UserLike.class);
        return userLike;
    }

    // 查询缘分值
    public Long queryScore(Long userId, Long recommendUserId) {
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("recommendUserId").is(recommendUserId)).limit(1);
        RecommendUser recommendUser = mongoTemplate.findOne(query, RecommendUser.class);
        if (recommendUser == null) {
            return 70L;
        }
        return recommendUser.getScore().longValue();
    }
}
