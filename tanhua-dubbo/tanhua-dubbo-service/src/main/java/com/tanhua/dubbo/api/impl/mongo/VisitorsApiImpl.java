package com.tanhua.dubbo.api.impl.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.mongo.Visitors;
import com.tanhua.dubbo.api.mongo.VisitorsApi;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Service
public class VisitorsApiImpl implements VisitorsApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    // 第一次访问，显示最近5位访客
    //db.visitors.find({userId:20}).sort({date:-1}).limit(5)
    @Override
    public List<Visitors> queryVisitors(Long userId, int top) {
        Query query = new Query(Criteria.where("userId").is(userId))
                .with(Sort.by(Sort.Order.desc("date")))
                .limit(top);
        return getVisitors(query);
    }

    // 第二次访问时候，显示最近的访客
    //db.visitors.find({userId:20,date:{$gt:100}})
    @Override
    public List<Visitors> queryVisitors(Long userId, Long time) {
        Query query = new Query(
                Criteria.where("userId").is(userId)
                        .and("date").gt(time)
        );
        query.with(Sort.by(Sort.Order.desc("date")));
        return getVisitors(query);
    }

    // 抽取公用方法
    private List<Visitors> getVisitors(Query query) {
        List<Visitors> visitorsList = mongoTemplate.find(query, Visitors.class);
        if (visitorsList != null && visitorsList.size()>0) {
            for (Visitors visitors : visitorsList) {
                Long score = this.queryScore(visitors.getUserId(), visitors.getVisitorUserId());
                visitors.setScore(score.doubleValue());
            }
        }
        return visitorsList;
    }

    // 查询缘分值
    public Long queryScore(Long userId, Long recommendUserId) {
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("recommendUserId").is(recommendUserId)).limit(1);
        RecommendUser recommendUser = mongoTemplate.findOne(query, RecommendUser.class);
        if (recommendUser == null ) {
            return 70L;
        }
        return recommendUser.getScore().longValue();
    }
}
