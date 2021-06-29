package com.tanhua.server.service;

import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.FreezeUser;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.vo.CommentVo;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.domain.vo.MovementsVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.dubbo.api.mongo.FreezeApi;
import com.tanhua.dubbo.api.mongo.PublishApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.RelativeDateFormat;
import org.apache.dubbo.config.annotation.Reference;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CommentService {

    @Reference
    private CommentApi commentApi;
    @Reference
    private PublishApi publishApi;
    @Reference
    private UserInfoApi userInfoApi;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Reference
    private FreezeApi freezeApi;

    /**
     * 接口名称：动态-点赞
     * 1、往quanzi_comment表插入一条数据：
     *  userId=谁点赞，登陆用户id
     *  commentType=1表示点赞。
     * 2、修改动态表的likeCount+1
     */
    public ResponseEntity<Object> likeComment(String publishId) {
        // 创建评论对象
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId));
        comment.setCommentType(1);
        comment.setPubType(1);
        comment.setUserId(UserHolder.getUserId());
        comment.setCreated(System.currentTimeMillis());

        // 保存点赞记录 (评论 )到mongo, 返回点赞数据
        long count = commentApi.save(comment);

        // 保存哪一个用户对哪个动态进行点赞的记录到redis
        String key = "public_like_comment_" + UserHolder.getUserId() + "_" + publishId;
        redisTemplate.opsForValue().set(key,"1");//标记。因为动态哪里会判断有这个标记说明点赞过（前端点亮图表）
        return ResponseEntity.ok(count);
    }

    /**
     * 接口名称：动态-取消点赞
     */
    public ResponseEntity<Object> dislikeComment(String publishId) {
        // 创建评论对象,封装数据
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId));
        comment.setCommentType(1);
        comment.setUserId(UserHolder.getUserId());

        // 执行删除评论数据
        long count = commentApi.delete(comment);

        // 清除redis中的标记
        String key = "public_like_comment_" + UserHolder.getUserId() + "_" + publishId;
        redisTemplate.delete(key);

        return ResponseEntity.ok(count);
    }

    /**
     * 接口名称：动态-喜欢
     */
    public ResponseEntity<Object> loveComment(String publishId) {
        // 创建评论对象
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId));
        comment.setCommentType(3); //3是喜欢
        comment.setPubType(1); //1是对动态操作
        comment.setUserId(UserHolder.getUserId());
        comment.setCreated(System.currentTimeMillis());

        // 保存点喜欢评论, 返回喜欢总数
        long count = commentApi.save(comment);

        // 保存哪一个用户对哪个动态进行喜欢的记录
        String key = "public_love_comment_" + UserHolder.getUserId() + "_" + publishId;
        //标记。因为动态哪里会判断有这个标记说明喜欢过（前端点亮图表）
        redisTemplate.opsForValue().set(key,"1");
        return ResponseEntity.ok(count);
    }

    /**
     * 接口名称：动态-取消喜欢
     */
    public ResponseEntity<Object> unloveComment(String publishId) {
        // 创建评论对象,封装数据
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId));
        comment.setCommentType(3);
        comment.setUserId(UserHolder.getUserId());

        // 执行删除评论数据
        long count = commentApi.delete(comment);

        // 清除redis中的标记
        String key = "public_love_comment_" + UserHolder.getUserId() + "_" + publishId;
        redisTemplate.delete(key);

        return ResponseEntity.ok(count);
    }

    /**
     * 接口名称：单条动态
     */
    public ResponseEntity<Object> queryMovementsById(String publishId) {
        //1. 根据动态id查询动态表
        Publish publish = publishApi.findById(publishId);

        //2. 创建MovementsVo，封装返回结果。 Publish-->MovementsVo
        MovementsVo vo = new MovementsVo();
        //封装数据：发布动态
        BeanUtils.copyProperties(publish,vo);
        //3.2.3 封装数据：先查询用户详情，再封装
        UserInfo userInfo = userInfoApi.findById(publish.getUserId());
        if (userInfo != null) {
            BeanUtils.copyProperties(userInfo,vo);
            if (userInfo.getTags() != null) {
                vo.setTags(userInfo.getTags().split(","));
            }
        }
        //封装数据：其他参数
        vo.setId(publish.getId().toString());
        vo.setUserId(publish.getUserId());
        vo.setImageContent(publish.getMedias().toArray(new String[]{}));
        vo.setDistance("50米");
        vo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));

        // 获取redis中存储的当前用户的点赞标记
        String key = "public_like_comment_" + UserHolder.getUserId() + "_" + vo.getId();
        if (redisTemplate.hasKey(key)) {
            vo.setHasLiked(1);  // 点赞过
        } else {
            vo.setHasLiked(0);  // 未赞过
        }

        String loveKey = "public_love_comment_" + UserHolder.getUserId() + "_" + vo.getId();
        if (redisTemplate.hasKey(loveKey)) {
            vo.setHasLoved(1);
        } else {
            vo.setHasLoved(0);
        }
        return ResponseEntity.ok(vo);
    }

    /**
     * 接口名称：评论列表
     * db.quanzi_comment.find({publishId:ObjectId('xx'),commentType:2})
     */
    public ResponseEntity<Object> queryCommentsList(String movementId, Integer page, Integer pagesize) {
        //1. 调用服务，查询评论列表
        PageResult pageResult = commentApi.queryCommentsList(movementId,page,pagesize);
        //2. 获取查询数据
        List<Comment> commentList = (List<Comment>) pageResult.getItems();

        //3. 创建返回的vo集合数据
        List<CommentVo> voList = new ArrayList<>();

        //4. 封装voList
        if (commentList != null) {
            for (Comment comment : commentList) {
                CommentVo vo = new CommentVo();
                UserInfo userInfo = userInfoApi.findById(comment.getUserId());
                if (userInfo != null) {
                    BeanUtils.copyProperties(userInfo,vo);
                }
                vo.setId(comment.getId().toString());

                vo.setContent(comment.getContent());
                vo.setCreateDate(new DateTime(comment.getCreated()).toString("yyyy年MM月dd日 HH:mm"));
                vo.setLikeCount(0);
                vo.setHasLiked(0);

                voList.add(vo);
            }
        }

        //5. 设置到分页对象中并返回
        pageResult.setItems(voList);

        return ResponseEntity.ok(pageResult);
    }

    /**
     * 接口名称：评论-提交
     */
    public ResponseEntity<Object> saveComments(String movementId, String commentText) {
        //判断是否被冻结
        UserInfo userInfo = userInfoApi.findById(UserHolder.getUserId());
        FreezeUser freezeUser = freezeApi.findByUserId(userInfo.getId().intValue());
        if(userInfo.getUserStatus() == 2 && freezeUser.getFreezingRange() == 2) {
            return ResponseEntity.status(500).body(ErrorResult.freezeChatError());
        }
        //1. 创建并封装保存的评论对象
        Comment c = new Comment();
        c.setPublishId(new ObjectId(movementId));
        c.setCommentType(2);
        c.setPubType(1);
        c.setContent(commentText);
        c.setUserId(UserHolder.getUserId());
        c.setCreated(System.currentTimeMillis());
        //2. 保存发布的评论
        commentApi.saveComment(c);
        return ResponseEntity.ok(null);
    }
}
