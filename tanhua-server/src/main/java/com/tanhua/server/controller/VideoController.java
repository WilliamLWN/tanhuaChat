package com.tanhua.server.controller;

import com.tanhua.server.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@Slf4j
public class VideoController {

    @Autowired
    private VideoService videoService;

    /**
     * 接口名称：小视频列表
     * 接口路径：GET/smallVideos
     */
    @GetMapping("/smallVideos")
    public ResponseEntity<Object> queryVideoList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pagesize) {
        log.info("接口名称：小视频列表");
        // 解决前端页码传入page=0的问题
        if (page < 1) page = 1;
        return ResponseEntity.ok(videoService.queryVideoList(page,pagesize));
    }

    /**
     * 接口名称：视频上传
     * 接口路径：POST/smallVideos
     * videoThumbnail 视频封面蹄片
     * videoFile 视频图片
     */
    @PostMapping("/smallVideos")
    public ResponseEntity<Object> uploadVideos(
            MultipartFile videoThumbnail, MultipartFile videoFile, String text) throws IOException {
        log.info("接口名称：视频上传");
        return videoService.uploadVideos(videoThumbnail,videoFile,text);
    }

    /**
     * 接口名称：视频用户关注
     * 接口路径：POST/smallVideos/:uid/userFocus
     */
    @PostMapping("/smallVideos/{uid}/userFocus")
    public ResponseEntity<Object> followUser(@PathVariable("uid") Long followUserId) {
        log.info("接口名称：视频用户关注");
        return videoService.followUser(followUserId);
    }

    /**
     * 接口名称：视频用户关注-取消
     * 接口路径：POST/smallVideos/:uid/userUnFocus
     */
    @PostMapping("/smallVideos/{uid}/userUnFocus")
    public ResponseEntity<Object> unfollowUser(@PathVariable("uid") Long followUserId) {
        log.info("接口名称：视频用户关注-取消");
        return videoService.unfollowUser(followUserId);
    }

    /**
     * 接口名称：视频点赞
     * 接口路径：POST/smallVideos/:id/like
     */
    @PostMapping("/smallVideos/{id}/dislike")
    public ResponseEntity<Object> likeVideo(@PathVariable("id") String videoId) {
        log.info("接口名称：视频点赞");
        return videoService.likeVideo(videoId);
    }

    /**
     * 接口名称：视频点赞 - 取消
     * 接口路径：POST/smallVideos/:id/dislike
     */
    @PostMapping("/smallVideos/{id}/like")
    public ResponseEntity<Object> dislikeVideo(@PathVariable("id") String videoId) {
        log.info("接口名称：视频点赞 - 取消");
        return videoService.dislikeVideo(videoId);
    }

    /**
     * 接口名称：小视频评论列表
     * 接口路径：GET/smallVideos/:id/comments
     */
    @GetMapping("/smallVideos/{id}/comments")
    public ResponseEntity<Object> comments(
            @PathVariable("id") String videoId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pagesize) {
        log.info("接口名称：小视频评论列表");
        return videoService.comments(videoId, page, pagesize);
    }

    /**
     * 接口名称：小视频评论发布
     * 接口路径：POST/smallVideos/:id/comments
     */
    @PostMapping("/smallVideos/{id}/comments")
    public ResponseEntity<Object> sendComments(
            @PathVariable("id") String videoId,
            @RequestBody Map<String,String> commentMap) {
        log.info("接口名称：小视频评论发布");
        return videoService.sendComments(videoId, commentMap);
    }
}
