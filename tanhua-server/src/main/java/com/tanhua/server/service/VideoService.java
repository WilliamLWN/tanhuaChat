package com.tanhua.server.service;

import cn.hutool.core.date.DateTime;
import com.aliyuncs.utils.StringUtils;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.FollowUser;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.CommentVideo;
import com.tanhua.domain.mongo.FreezeUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.CommentVo;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.VideoVo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.dubbo.api.mongo.FreezeApi;
import com.tanhua.dubbo.api.mongo.VideoApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class VideoService {

    // 注入dubbo服务接口的代理对象
    @Reference
    private VideoApi videoApi;
    @Reference
    private UserInfoApi userInfoApi;
    @Reference
    private CommentApi commentApi;
    @Reference
    private FreezeApi freezeApi;
    @Autowired
    private OssTemplate ossTemplate;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private VideoMqService videoMqService;
//    @Autowired
//    private FastFileStorageClient storageClient;
//    @Autowired
//    private FdfsWebServer fdfsWebServer;

    /**
     * 接口名称：小视频列表
     * 修改VideoService，实现分页列表加入缓存
     */
    @Cacheable(value = "videoList",key = "#page + '_' + #pagesize")
    public PageResult queryVideoList(Integer page, Integer pagesize) {
        log.info("---------------->小视频列表查询数据库！");

        //1、调用api分页查询小视频---------------------------------------------------------
//        PageResult pageResult = this.findRecommend(page,pagesize,UserHolder.getUserId());
//        if(pageResult == null) {
          PageResult  pageResult = videoApi.findByPage(page,pagesize);
//        }
        //---------------------------------------------------------------------------------
        List<Video> videoList = (List<Video>) pageResult.getItems();

        //2、创建返回的vo集合对象
        List<VideoVo> voList = new ArrayList<>();

        //3. 封装数据
        if (videoList != null) {
            for (Video video : videoList) {
                //3.1 创建vo对象，封装前端返回的数据
                VideoVo vo = new VideoVo();
                BeanUtils.copyProperties(video,vo);
                //3.2 根据用户id查询用户并封装
                UserInfo userInfo = userInfoApi.findById(video.getUserId());
                if (userInfo!= null) {
                    BeanUtils.copyProperties(userInfo,vo);
                }
                //3.3 设置小视频封面
                vo.setCover(video.getPicUrl());
                //3.4 设置id、签名
                vo.setId(video.getId().toString());
                vo.setSignature(video.getText());
                vo.setHasLiked(0);

                String key = "followUser_" + UserHolder.getUserId() + "_" + vo.getUserId();
                if (redisTemplate.hasKey(key)) {
                    vo.setHasFocus(1);// 已关注此视频
                } else {
                    vo.setHasFocus(0);
                }

                //3.5 vo添加到集合
                voList.add(vo);
            }
        }

        //4. 设置到PageResult中
        pageResult.setItems(voList);

        //5. 响应
        return pageResult;
    }

    /**
     * 接口名称：视频上传
     * 实现发布视频时候删除缓存
     */
    @CacheEvict(value = "videoList",allEntries = true)
    public ResponseEntity<Object> uploadVideos(MultipartFile videoThumbnail, MultipartFile videoFile, String text) throws IOException {
        //1. 视频封面（videoThumbnail） 上传到阿里云
        String picUrl = ossTemplate.upload(videoThumbnail.getOriginalFilename(), videoThumbnail.getInputStream());
        System.out.println("picUrl = " + picUrl);

        //2. 视频上传到fastdfs
//        String videoFileName = videoFile.getOriginalFilename();
//        String ext = videoFileName.substring(videoFileName.lastIndexOf(".") + 1);
//        StorePath storePath = storageClient.uploadFile(videoFile.getInputStream(), videoFile.getSize(), ext, null);
//        String videoUrl = fdfsWebServer.getWebServerUrl() + storePath.getFullPath();

        //2.视频上传到阿里云
        String videoFileName = videoFile.getOriginalFilename();
        String videoUrl = ossTemplate.uploadFile(videoFileName, videoFile.getInputStream());
        System.out.println("videoUrl = " + videoUrl);

        //3. 保存video
        Video video = new Video();
        //[设置主键id]
        video.setId(ObjectId.get());
        video.setCreated(System.currentTimeMillis());
        video.setUserId(UserHolder.getUserId());
        if (StringUtils.isEmpty(text)) {
            video.setText("文本自动补全！");
        }else{
            video.setText(text);
        }
        video.setPicUrl(picUrl);
        video.setVideoUrl(videoUrl);

        videoApi.save(video);
        System.out.println("video = " + video);
        // [发送mq消息]
        videoMqService.videoMsg(video.getId().toString());
        return ResponseEntity.ok(null);
    }

    /**
     * 接口名称：视频用户关注
     * @param followUserId
     * @return
     */
    public ResponseEntity<Object> followUser(Long followUserId) {
        //1. 保存关注用户数据到follow_user表中
        FollowUser followUser = new FollowUser();
        followUser.setCreated(System.currentTimeMillis());
        followUser.setUserId(UserHolder.getUserId());
        followUser.setFollowUserId(followUserId);
        videoApi.followUser(followUser);

        //2. 记录关注的标记（前端点亮图表）
        String key = "followUser_" + UserHolder.getUserId() + "_" + followUser.getFollowUserId();
        redisTemplate.opsForValue().set(key,"1");
        return ResponseEntity.ok(null);
    }

    /**
     * 接口名称：视频用户关注 - 取消
     */
    public ResponseEntity<Object> unfollowUser(Long followUserId) {
        //1. 获取用户id
        Long userId = UserHolder.getUserId();
        //2. 取消关注
        videoApi.unfollowUser(userId,followUserId);
        //3. 删除redis中关注标记
        String key = "followUser_"+userId+"_"+followUserId;
        redisTemplate.delete(key);
        return ResponseEntity.ok(null);
    }

    public PageResult findRecommend(Integer page, Integer pagesize, Long userId) {
        PageResult result = null;
        String key = "QUANZI_VIDEO_RECOMMEND_" + userId;
        String value = redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        String[] pids = value.split(",");
        Integer counts = pids.length;
        int startIndex = (page - 1) * pagesize;

        if (startIndex < pids.length) {
            int endIndex = startIndex + pagesize - 1;
            if (endIndex >= pids.length) {
                endIndex = pids.length - 1;
            }

            List<Long> vidList = new ArrayList<>();
            for (int i = startIndex; i <= endIndex; i++) {
                vidList.add(Long.valueOf(pids[i]));
            }

            List<Video> videoList = this.videoApi.queryVideoListByPids(vidList);
            result = new PageResult(page, pagesize, counts, videoList);
        }

        return result;
    }

    /**
     * 接口名称：视频点赞
     * @param likeVideoId
     * @return
     */
    public ResponseEntity<Object> likeVideo(String likeVideoId) {
        // 创建评论对象
        CommentVideo commentVideo = new CommentVideo();
        commentVideo.setVideoId(new ObjectId(likeVideoId));
        commentVideo.setCommentType(1); //1是点赞
        commentVideo.setPubType(1); //1是对动态操作
        commentVideo.setUserId(UserHolder.getUserId());
        commentVideo.setCreated(System.currentTimeMillis());

        // 保存点喜欢评论, 返回喜欢总数
        long count = commentApi.saveVideolikeComment(commentVideo);

        // 保存哪一个用户对哪个动态进行喜欢的记录
        String key = "public_like_videoComment_" + UserHolder.getUserId() + "_" + likeVideoId;
        //标记。因为小视频哪里会判断有这个标记说明喜欢过（前端点亮图表）
        redisTemplate.opsForValue().set(key,"1");
        return ResponseEntity.ok(count);
    }


    /**
     * 接口名称：视频点赞 - 取消
     * @param videoId
     * @return
     */
    public ResponseEntity<Object> dislikeVideo(String videoId) {
        String key = "public_like_videoComment_" + UserHolder.getUserId() + "_" + videoId;
        if(!redisTemplate.hasKey(key)) {
            return null;
        }

        // 创建评论对象,封装数据
        CommentVideo commentVideo = new CommentVideo();
        commentVideo.setVideoId(new ObjectId(videoId));
        commentVideo.setCommentType(1);
        commentVideo.setUserId(UserHolder.getUserId());

        // 执行删除评论数据
        long count = commentApi.deleteVideoLike(commentVideo);

        // 清除redis中的标记
        redisTemplate.delete(key);

        return ResponseEntity.ok(count);
    }

    /**
     * 接口名称：小视频评论列表
     * @param videoId
     * @return
     */
    public ResponseEntity<Object> comments(String videoId, Integer page, Integer pagesize) {
        //1. 调用服务，查询评论列表
        PageResult pageResult = commentApi.queryVideoCommentsList(videoId,page,pagesize);
        //2. 获取查询数据
        List<CommentVideo> commentVideoList = (List<CommentVideo>) pageResult.getItems();

        //3. 创建返回的vo集合数据
        List<CommentVo> voList = new ArrayList<>();

        //4. 封装voList
        if (commentVideoList != null) {
            for (CommentVideo commentVideo : commentVideoList) {
                CommentVo vo = new CommentVo();
                UserInfo userInfo = userInfoApi.findById(commentVideo.getUserId());
                vo.setId(userInfo.getId().toString());
                vo.setAvatar(userInfo.getAvatar());
                vo.setNickname(userInfo.getNickname());
                vo.setContent(commentVideo.getContent());
                vo.setCreateDate(new DateTime(commentVideo.getCreated()).toString("yyyy年MM月dd日 HH:mm"));
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
     * 接口名称：小视频评论发布
     * @param videoId
     * @param commentMap
     * @return
     */
    public ResponseEntity<Object> sendComments(String videoId, Map<String,String> commentMap) {
        //判断是否被冻结
        UserInfo userInfo = userInfoApi.findById(UserHolder.getUserId());
        FreezeUser freezeUser = freezeApi.findByUserId(userInfo.getId().intValue());
        if(userInfo.getUserStatus() == 2 && freezeUser.getFreezingRange() == 2) {
            return ResponseEntity.status(500).body(ErrorResult.freezeChatError());
        }

        String comment = commentMap.get("comment");

        //1. 创建并封装保存的评论对象
        CommentVideo c = new CommentVideo();
        c.setVideoId(new ObjectId(videoId));
        c.setCommentType(2);
        c.setPubType(1);
        c.setContent(comment);
        c.setUserId(UserHolder.getUserId());
        c.setCreated(System.currentTimeMillis());
        //2. 保存发布的评论
        commentApi.saveVideoComment(c);
        return ResponseEntity.ok(null);
    }
}
