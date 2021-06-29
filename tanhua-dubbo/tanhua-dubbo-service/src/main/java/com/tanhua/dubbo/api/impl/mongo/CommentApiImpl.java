package com.tanhua.dubbo.api.impl.mongo;

import com.tanhua.domain.mongo.*;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.mongo.CommentApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@Service(timeout = 500000)
@Slf4j
public class CommentApiImpl implements CommentApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public long save(Comment comment) {
        //1、保存评论；db_quanzi_comment
        comment.setId(ObjectId.get());
        mongoTemplate.save(comment);

        //2、返回点赞数量；查询Comment
        Query commentQuery = new Query(Criteria.where("publishId").is(comment.getPublishId()).and("commentType").is(comment.getCommentType())
        );
        long count = mongoTemplate.count(commentQuery, Comment.class);

        //3、修改发布动态表,点赞数量+1；quanzi_publish
        //3.1 修改条件：动态id
        Query query = new Query(Criteria.where("id").is(comment.getPublishId()));
        //3.2 修改内容：如likeCount+1
        Update update = new Update();
        //comment.getCol()相当于update.inc("likeCount",1);
        update.inc(comment.getCol(), 1);
        mongoTemplate.updateFirst(query, update, Publish.class);

        return count;
    }

    @Override
    public long delete(Comment comment) {
        //1、执行删除评论数据
        Query query = new Query(
                Criteria.where("publishId").is(comment.getPublishId())
                        .and("commentType").is(comment.getCommentType())
                        .and("userId").is(comment.getUserId())
        );
        mongoTemplate.remove(query, Comment.class);

        //2、修改发布动态表，点赞数量减1
        Query publisQuery = new Query(Criteria.where("id").is(comment.getPublishId()));
        Update update = new Update();
        update.inc(comment.getCol(), -1);
        mongoTemplate.updateFirst(publisQuery, update, Publish.class);

        //3、查询点赞总数并返回
        Query commentQuery = new Query(
                Criteria.where("publishId").is(comment.getPublishId()).and("commentType").is(comment.getCommentType())
        );
        long count = mongoTemplate.count(commentQuery, Comment.class);
        return count;
    }

    //接口名称：评论列表
    //db.quanzi_comment.find({publishId:ObjectId('xx'),commentType:2}).sort({created:-1})
    @Override
    public PageResult queryCommentsList(String publishId, Integer page, Integer pagesize) {
        //1. 创建查询对象
        Query query = new Query(Criteria.where("publishId").is(new ObjectId(publishId)).and("commentType").is(2));
        query.with(Sort.by(Sort.Order.desc("created")));
        query.limit(pagesize).skip((page - 1) * pagesize);

        //2. 分页查询
        List<Comment> commentList = mongoTemplate.find(query, Comment.class);
        long count = mongoTemplate.count(query, Comment.class);

        //3. 封装分页结果并返回
        return new PageResult(page, pagesize, (int) count, commentList);
    }

    @Override
    public void saveComment(Comment comment) {
        //1、保存评论；db_quanzi_comment
        comment.setId(ObjectId.get());
        mongoTemplate.save(comment);

        //3、修改发布动态表,点赞数量+1；quanzi_publish
        //3.1 修改条件：动态id
        Query query = new Query(Criteria.where("id").is(comment.getPublishId()));
        //3.2 修改内容：如likeCount+1
        Update update = new Update();
        //comment.getCol()相当于update.inc("likeCount",1);
        update.inc(comment.getCol(), 1);
        mongoTemplate.updateFirst(query, update, Publish.class);
    }

    @Override
    public PageResult findLikesByUserId(Integer page, Integer pagesize, Long userId) {
        //1. 创建查询对象
        Query query = new Query(Criteria.where("userId").is(userId));
        List<Publish> publishes = mongoTemplate.find(query, Publish.class);
        for (Publish publish : publishes) {
            ObjectId publishId = publish.getId();
            if (publishId != null) {
                query = new Query(Criteria.where("publishId").is(publishId).and("commentType").is(1));
                List<Comment> commentList = mongoTemplate.find(query, Comment.class);
                if (commentList != null) {
                    for (Comment comment : commentList) {
                        LikesPublish likesPublish = new LikesPublish();
                        BeanUtils.copyProperties(comment, likesPublish);
                        query = new Query(Criteria.where("publishId").is(likesPublish.getPublishId()).and("userId").is(likesPublish.getUserId()));
                        if (!mongoTemplate.exists(query, LikesPublish.class)) {
                            mongoTemplate.save(likesPublish);
                        }
                    }
                } else {
                    return null;
                }
            }
        }

        Query query1 = new Query();
        query1.with(Sort.by(Sort.Order.desc("created")));
        query1.limit(pagesize).skip((page - 1) * pagesize);
        List<LikesPublish> likesPublishList = mongoTemplate.find(query1, LikesPublish.class);

        //2. 分页查询
        long count = mongoTemplate.count(query1, LikesPublish.class);

        //3. 封装分页结果并返回
        return new PageResult(page, pagesize, (int) count, likesPublishList);
    }

    @Override
    public PageResult findCommentsByUserId(Integer page, Integer pagesize, Long userId) {
        //1. 创建查询对象
        Query query = new Query(Criteria.where("userId").is(userId));
        List<Publish> publishes = mongoTemplate.find(query, Publish.class);
        for (Publish publish : publishes) {
            ObjectId publishId = publish.getId();
            if (publishId != null) {
                query = new Query(Criteria.where("publishId").is(publishId).and("commentType").is(2));
                List<Comment> commentList = mongoTemplate.find(query, Comment.class);
                if (commentList != null) {
                    for (Comment comment : commentList) {
                        CommentPublish commentPublish = new CommentPublish();
                        if (!comment.getUserId().equals(userId)) {
                            BeanUtils.copyProperties(comment, commentPublish);
                            query = new Query(Criteria.where("publishId").is(commentPublish.getPublishId()).and("userId").is(commentPublish.getUserId()));
                            if (!mongoTemplate.exists(query, CommentPublish.class)) {
                                mongoTemplate.save(commentPublish);
                            }
                        }
                    }
                } else {
                    return null;
                }
            }
        }

        Query query1 = new Query();
        query1.with(Sort.by(Sort.Order.desc("created")));
        query1.limit(pagesize).skip((page - 1) * pagesize);
        List<CommentPublish> commentPublishList = mongoTemplate.find(query1, CommentPublish.class);

        //2. 分页查询
        long count = mongoTemplate.count(query1, CommentPublish.class);

        //3. 封装分页结果并返回
        return new PageResult(page, pagesize, (int) count, commentPublishList);
    }

    @Override
    public PageResult findLovesByUserId(Integer page, Integer pagesize, Long userId) {
        //1. 创建查询对象
        Query query = new Query(Criteria.where("userId").is(userId));
        List<Publish> publishes = mongoTemplate.find(query, Publish.class);
        for (Publish publish : publishes) {
            ObjectId publishId = publish.getId();
            if (publishId != null) {
                query = new Query(Criteria.where("publishId").is(publishId).and("commentType").is(3));
                List<Comment> commentList = mongoTemplate.find(query, Comment.class);
                if (commentList != null) {
                    for (Comment comment : commentList) {
                        LovesPublish lovesPublish = new LovesPublish();
                        BeanUtils.copyProperties(comment, lovesPublish);
                        query = new Query(Criteria.where("publishId").is(lovesPublish.getPublishId()).and("userId").is(lovesPublish.getUserId()));
                        if (!mongoTemplate.exists(query, LovesPublish.class)) {
                            mongoTemplate.save(lovesPublish);
                        }
                    }
                } else {
                    return null;
                }
            }
        }

        Query query1 = new Query();
        query1.with(Sort.by(Sort.Order.desc("created")));
        query1.limit(pagesize).skip((page - 1) * pagesize);
        List<LovesPublish> lovesPublishList = mongoTemplate.find(query1, LovesPublish.class);

        //2. 分页查询
        long count = mongoTemplate.count(query1, LovesPublish.class);

        //3. 封装分页结果并返回
        return new PageResult(page, pagesize, (int) count, lovesPublishList);
    }

    @Override
    public long saveVideolikeComment(CommentVideo commentVideo) {
        //1、保存小视频评论；db_quanzi_comment
        commentVideo.setId(ObjectId.get());
        mongoTemplate.save(commentVideo);

        //2、返回点赞数量；查询Comment
        Query commentQuery = new Query(Criteria.where("videoId").is(commentVideo.getVideoId()).and("commentType").is(commentVideo.getCommentType())
        );
        long count = mongoTemplate.count(commentQuery, CommentVideo.class);

        //3、修改发布小视频表,点赞数量+1；video
        //3.1 修改条件：小视频id
        Query query = new Query(Criteria.where("id").is(commentVideo.getVideoId()));
        //3.2 修改内容：如likeCount+1
        Update update = new Update();
        //comment.getCol()相当于update.inc("likeCount",1);
        update.inc("likeCount", 1);
        mongoTemplate.updateFirst(query, update, Video.class);

        return count;
    }

    @Override
    public long deleteVideoLike(CommentVideo commentVideo) {
        //1、执行删除评论数据
        Query query = new Query(
                Criteria.where("videoId").is(commentVideo.getVideoId())
                        .and("commentType").is(commentVideo.getCommentType())
                        .and("userId").is(commentVideo.getUserId())
        );
        mongoTemplate.remove(query, CommentVideo.class);

        //2、修改发布动态表，点赞数量减1
        Query publisQuery = new Query(Criteria.where("id").is(commentVideo.getVideoId()));
        Update update = new Update();
        update.inc("likeCount", -1);
        mongoTemplate.updateFirst(publisQuery, update, Video.class);

        //3、查询点赞总数并返回
        Query commentQuery = new Query(
                Criteria.where("videoId").is(commentVideo.getVideoId()).and("commentType").is(commentVideo.getCommentType())
        );
        return mongoTemplate.count(commentQuery, CommentVideo.class);
    }

    @Override
    public PageResult queryVideoCommentsList(String videoId, Integer page, Integer pagesize) {
        //1. 创建查询对象
        Query query = new Query(Criteria.where("videoId").is(new ObjectId(videoId)).and("commentType").is(2));
        query.with(Sort.by(Sort.Order.desc("created")));
        query.limit(pagesize).skip((page - 1) * pagesize);

        //2. 分页查询
        List<CommentVideo> commentVideoList = mongoTemplate.find(query, CommentVideo.class);
        long count = mongoTemplate.count(query, CommentVideo.class);

        //3. 封装分页结果并返回
        return new PageResult(page, pagesize, (int) count, commentVideoList);
    }

    @Override
    public void saveVideoComment(CommentVideo commentVideo) {
        //1、保存小视频评论；db_quanzi_comment
        commentVideo.setId(ObjectId.get());
        mongoTemplate.save(commentVideo);

        //3、修改发布小视频表,评论数量+1；video
        //3.1 修改条件：小视频id
        Query query = new Query(Criteria.where("id").is(commentVideo.getVideoId()));
        //3.2 修改内容：如likeCount+1
        Update update = new Update();
        //comment.getCol()相当于update.inc("likeCount",1);
        update.inc("commentCount", 1);
        mongoTemplate.updateFirst(query, update, Video.class);
    }
}
