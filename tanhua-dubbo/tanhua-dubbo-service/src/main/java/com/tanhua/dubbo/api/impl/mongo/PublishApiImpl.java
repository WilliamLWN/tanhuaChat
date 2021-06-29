package com.tanhua.dubbo.api.impl.mongo;

import com.tanhua.domain.mongo.*;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.mongo.PublishApi;
import com.tanhua.dubbo.utils.IdService;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.List;

@Service(timeout = 100000)
public class PublishApiImpl implements PublishApi {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IdService idService;

    @Override
    public void save(Publish publish) {

        //发布动态（1）首先将发布的动态内容写入到发布表中  publish
//        publish.setId(ObjectId.get());  service已经设置了
        publish.setCreated(System.currentTimeMillis());
        //[设置pid，用于大数据推荐计算]
        publish.setPid(idService.getNextId("quanzi_publish"));  // 这里添加pid
        mongoTemplate.save(publish);

        //发布动态（2）再将发布的内容保存到自己的相册表（圈子） album
        //比如：登陆用户是20，相册表就是quanzi_album_20
        Album album = new Album();
        album.setId(ObjectId.get());
        album.setPublishId(publish.getId());
        album.setCreated(publish.getCreated());
        mongoTemplate.save(album, "quanzi_album_" + publish.getUserId());

        //发布动态（3）最后，根据userid查找好友，将发布的动态保存到每个好友的时间线表中 time_line
        //比如用户的好友是：2、3
        //对应的时间线表是：time_lime_2、time_lime_3
        //所以需要往上面2个表写入发布的动态
        Query query = new Query(Criteria.where("userId").is(publish.getUserId()));
        List<Friend> friendList = mongoTemplate.find(query, Friend.class);
        for (Friend friend : friendList) {
            Long friendId = friend.getFriendId();

            TimeLine timeLine = new TimeLine();
            timeLine.setId(ObjectId.get());
            timeLine.setPublishId(publish.getId());
            timeLine.setUserId(publish.getUserId());
            timeLine.setCreated(publish.getCreated());
            mongoTemplate.save(timeLine, "quanzi_time_line_" + friendId);
        }

    }

    @Override
    public PageResult findByTimeLine(Integer page, Integer pagesize, Long userId) {
        // 1、查询时间线表
        Query query = new Query();
        query.with(Sort.by(Sort.Order.desc("created")));
        query.limit(pagesize).skip((page - 1) * pagesize);
        List<TimeLine> timeLineList = mongoTemplate.find(query, TimeLine.class, "quanzi_time_line_" + userId);
        long count = mongoTemplate.count(query, TimeLine.class, "quanzi_time_line_" + userId);

        // 2、返回发布动态的集合（因为在service要封装MovementsVo，而movementsVo需要获取发布数据）
        List<Publish> publishList = new ArrayList<>();
        if (timeLineList != null) {
            for (TimeLine timeLine : timeLineList) {
                if (timeLine.getPublishId() != null) {
                    // 根据发布动态id查询
                    Publish publish =
                            mongoTemplate.findById(timeLine.getPublishId(), Publish.class);
                    // 添加到集合
                    if (publish != null) {
                        // 【state=1 已审核，才可以显示推荐动态。
                        // 审核动态交给消息处理系统tanhua-manage】
                        if (publish.getState() != null && publish.getState() == 1) {//添加的判断
                            publishList.add(publish);
                        }
                    }
                }
            }
        }
        //3、创建分页对象、封装结果并返回
        return new PageResult(page, pagesize, (int) count, publishList);
    }

    @Override
    public PageResult queryRecommendPublishList(Integer page, Integer pagesize, Long userId) {
        // 1、查询推荐表 ： db.recommend_quanzi.find({userId:20})
        Query query = new Query(Criteria.where("userId").is(userId));
        query.with(Sort.by(Sort.Order.desc("created")));
        query.limit(pagesize).skip((page - 1) * pagesize);

        List<RecommendQuanzi> quanziList = mongoTemplate.find(query, RecommendQuanzi.class);
        long count = mongoTemplate.count(query, RecommendQuanzi.class);

        // 2、返回发布动态的集合（因为在service要封装MovementsVo，而movementsVo需要获取发布数据）
        List<Publish> publishList = new ArrayList<>();
        if (quanziList != null) {
            for (RecommendQuanzi recommendQuanzi : quanziList) {
                if (recommendQuanzi.getPublishId() != null) {
                    //System.out.println("recommendQuanzi.getPublishId() = " + recommendQuanzi.getPublishId());
                    // 根据发布动态id查询
                    Publish publish = mongoTemplate.findById(recommendQuanzi.getPublishId(), Publish.class);
                    //System.out.println("publish = " + publish);
                    // 添加到集合
                    if (publish != null) {
                        // 【state=1 已审核，才可以显示推荐动态。
                        // 审核动态交给消息处理系统tanhua-manage】
                        if (publish.getState() != null && publish.getState() ==1) {//添加的判断
                            publishList.add(publish);
                        }
                    }
                }
            }
        }
        //3、创建分页对象、封装结果并返回
        return new PageResult(page, pagesize, (int) count, publishList);
    }

    @Override
    public PageResult queryMyAlbumList(Integer page, Integer pagesize, Long userId) {
        // 1、查询推荐表 ： db.recommend_quanzi.find({userId:20})
        Query query = new Query();
        query.with(Sort.by(Sort.Order.desc("created")));
        query.limit(pagesize).skip((page - 1) * pagesize);

        List<Album> albumList = mongoTemplate.find(query, Album.class, "quanzi_album_" + userId);
        long count = mongoTemplate.count(query, Album.class, "quanzi_album_" + userId);
        // 2、返回发布动态的集合（因为在service要封装MovementsVo，而movementsVo需要获取发布数据）
        List<Publish> publishList = new ArrayList<>();
        if (albumList != null) {
            for (Album album : albumList) {
                if (album.getPublishId() != null) {
                    // 根据发布动态id查询
                    Publish publish = mongoTemplate.findById(album.getPublishId(), Publish.class);
                    System.out.println("publish = " + publish);
                    // 添加到集合
                    if (publish != null) {
                        // 【state=1 已审核，才可以显示推荐动态。
                        // 审核动态交给消息处理系统tanhua-manage】
                        if (publish.getState() != null && publish.getState() == 1) {//添加的判断
                            publishList.add(publish);
                        }
                    }
                }
            }
        }
        //3、创建分页对象、封装结果并返回
        return new PageResult(page, pagesize, (int) count, publishList);
    }

    @Override
    public Publish findById(String publishId) {
        return mongoTemplate.findById(new ObjectId(publishId), Publish.class);
    }

    @Override
    public PageResult findPublishList(Integer page, Integer pagesize, Long uid, Long state) {
        Query query = new Query();
        if (uid != null) {
            query.addCriteria(Criteria.where("userId").is(uid));
        }
        query.limit(pagesize).skip((page - 1) * pagesize);
        List<Publish> list = mongoTemplate.find(query, Publish.class);
        long count = mongoTemplate.count(query, Publish.class);
        return new PageResult(page, pagesize, (int) count, list);
    }

    @Override
    public void updateState(String publishId, Integer state) {
        Query query = new Query(Criteria.where("id").is(new ObjectId(publishId)));
        Update update = new Update();
        update.set("state", state);
        mongoTemplate.updateFirst(query, update, Publish.class);
    }

    @Override
    public List<Publish> findByPids(List<Long> pidList) {
        Query query = Query.query(Criteria.where("pid").in(pidList));
        return mongoTemplate.find(query, Publish.class);
    }
}
