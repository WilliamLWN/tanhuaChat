package com.tanhua.dubbo.api.impl.mongo;

import com.tanhua.domain.db.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.mongo.VideoApi;
import com.tanhua.dubbo.utils.IdService;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Service
public class VideoApiImpl implements VideoApi {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IdService idService;

    @Override
    public PageResult findByPage(Integer page, Integer pagesize) {
        // 分页查询小视频、按照创建时间降序
        Query query = new Query();
        query.limit(pagesize).skip((page - 1) * pagesize);
        query.with(Sort.by(Sort.Order.desc("created")));
        List<Video> videoList = mongoTemplate.find(query, Video.class);
        long count = mongoTemplate.count(query, Video.class);
        return new PageResult(page, pagesize, (int) count, videoList);
    }

    @Override
    public void save(Video video) {
        video.setVid(idService.getNextId("video"));
        mongoTemplate.save(video);
    }

    @Override
    public void followUser(FollowUser followUser) {
        mongoTemplate.save(followUser);
    }

    @Override
    public void unfollowUser(Long userId, Long followUserId) {
        Query query = new Query(Criteria.where("userId").is(userId).and("followUserId").is(followUserId));
        mongoTemplate.remove(query,FollowUser.class);
    }

    @Override
    public PageResult findByPage(Integer page, Integer pagesize, Long uid) {
        Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(uid));
        query.limit(pagesize).skip((page - 1) * pagesize);
        query.with(Sort.by(Sort.Order.desc("created")));
        List<Video> videoList = mongoTemplate.find(query, Video.class);
        long count = mongoTemplate.count(query, Video.class);
        return new PageResult(page, pagesize, (int) count, videoList);
    }

    //根据id查询
    @Override
    public Video findById(String videoId) {
        return mongoTemplate.findById(new ObjectId(videoId), Video.class);
    }

    @Override
    public List<Video> queryVideoListByPids(List<Long> vidList) {
        Query query = new Query(Criteria.where("vid").in(vidList));
        return mongoTemplate.find(query,Video.class);
    }
}
