package com.tanhua.dubbo.api.impl.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.mongo.RecommendUserApi;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Service(timeout = 100000)
public class RecommendUserApiImpl implements RecommendUserApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public RecommendUser queryWithMaxScore(Long userId) {

        //db.recommend_user.find({userId:20}).sort({score:-1}).limit(1)
        Query query = new Query(Criteria.where("userId").is(userId));
        query.with(Sort.by(Sort.Order.desc("score")));
        query.limit(1);

        return mongoTemplate.findOne(query, RecommendUser.class);
    }

    @Override
    public PageResult queryRecommendation(Long userId, Integer page, Integer pagesize) {
        //1. 查询首页推荐，构造查询条件
        //db.recommend_user.find({userId:20}).sort({score:-1}).limit(5).skip(0)
        //db.recommend_user.find({userId:20}).sort({score:-1}).limit(5).skip(5)
        //db.recommend_user.find({userId:20}).sort({score:-1}).limit(5).skip(10)
        Query query = new Query(Criteria.where("userId").is(userId));
        query.with(Sort.by(Sort.Order.desc("score")));
        query.limit(pagesize).skip((page-1)*pagesize);
        //查询数据
        List<RecommendUser> recommendUsers = mongoTemplate.find(query, RecommendUser.class);
        //获取总条数
        long counts = mongoTemplate.count(query, RecommendUser.class);
        return new PageResult(page, pagesize, (int)counts, recommendUsers);
    }

    @Override
    public long queryScore(Long userId, Long recommendUserId) {
        Query query = new Query(Criteria.where("userId").is(userId).and("recommendUserId").is(recommendUserId));
        RecommendUser recommendUser =
                mongoTemplate.findOne(query, RecommendUser.class);
        if (recommendUser == null) {
            return 80L;
        }
        return recommendUser.getScore().longValue();
    }
}
