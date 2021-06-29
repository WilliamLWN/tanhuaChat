package com.tanhua.server.controller;

import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.server.service.CommentService;
import com.tanhua.server.service.MovementsMQService;
import com.tanhua.server.service.MovementsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/movements")
@Slf4j
public class MovementsController {

    @Autowired
    private MovementsService movementsService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private MovementsMQService movementsMQService;

    /**
     * 接口名称：动态-发布
     * 接口路径：POST/movements
     */
    @PostMapping
    public ResponseEntity<Object> saveMovements(Publish publish, MultipartFile[] imageContent) throws Exception {
        log.info("接口名称：动态-发布");
        ResponseEntity<Object> entity = movementsService.saveMovements(publish, imageContent);
        if(entity == null) {
            return ResponseEntity.status(500).body(ErrorResult.freezePublishError());
        }
        // [发送mq消息，用于推荐计算]
        movementsMQService.publishMsg(publish.getId().toString());
        return entity;
    }

    /**
     * 接口名称：好友动态
     * 接口路径：GET/movements
     * 需求描述：查询好友动态，就是查看“朋友圈”。在自己的时间线表中存储了所有好友的动态
     * 举例：登陆用户id是20，查询好友动态就是查询 db.quanzi_time_line_20.find()
     */
    @GetMapping
    public ResponseEntity<Object> queryPublishList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pagesize) throws IOException {
        if (page < 1) page = 1;
        log.info("接口名称：好友动态");
        return movementsService.queryPublishList(page, pagesize);
    }

    /**
     * 接口名称：推荐动态
     * 接口路径：GET/movements/recommend
     * 需求描述：查询推荐动态，查询  recommend_quanzi 表
     */
    @GetMapping("/recommend")
    public ResponseEntity<Object> queryRecommendPublishList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pagesize) throws IOException {
        if (page < 1) page = 1;
        log.info("接口名称：推荐动态");
        return movementsService.queryRecommendPublishList(page, pagesize);
    }

    /**
     * 接口名称：我的动态
     * 接口路径：GET/movements/all
     * 需求描述：查询好友动态，就是查看“朋友圈”。在自己的时间线表中存储了所有好友的动态
     * 举例：登陆用户id是20，查询好友动态就是查询 db.quanzi_time_line_20.find()
     * 测试过，前端根本不会返回id
     */
    @GetMapping("/all")
    public ResponseEntity<Object> queryMyAlbumList(
            String id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pagesize) throws IOException {
        if (page < 1) page = 1;
        log.info("接口名称：我的动态");
        return movementsService.queryMyAlbumList(page, pagesize, id);
    }

    /**
     * 接口名称：动态-点赞
     * 接口路径：GET/movements/:id/like
     */
    @GetMapping("/{id}/like")
    public ResponseEntity<Object> likeComment(@PathVariable("id") String publishId) {
        log.info("接口名称：动态-点赞");
        // [发送mq消息，用于推荐计算]
        movementsMQService.likePublishMsg(publishId);
        return commentService.likeComment(publishId);
    }

    /**
     * 接口名称：动态-取消点赞
     * 接口路径：GET/movements/:id/dislike
     */
    @GetMapping("{id}/dislike")
    public ResponseEntity<Object> dislikeComment(@PathVariable("id") String publishId) {
        log.info("接口名称：动态-取消点赞");
        // [发送mq消息，用于推荐计算]
        movementsMQService.disLikePublishMsg(publishId);
        return commentService.dislikeComment(publishId);
    }

    /**
     * 接口名称：动态-喜欢
     * 接口路径：GET/movements/:id/love
     */
    @GetMapping("{id}/love")
    public ResponseEntity<Object> loveComment(@PathVariable("id") String publishId) {
        log.info("接口名称：动态-喜欢");
        // [发送mq消息，用于推荐计算]
        movementsMQService.lovePublishMsg(publishId);
        return commentService.loveComment(publishId);
    }

    /**
     * 接口名称：动态-取消喜欢
     * 接口路径：GET/movements/:id/unlove
     */
    @GetMapping("/{id}/unlove")
    public ResponseEntity<Object> unloveComment(@PathVariable("id") String publishId) {
        log.info("接口名称：动态-取消喜欢");
        // [发送mq消息，用于推荐计算]
        movementsMQService.disLovePublishMsg(publishId);
        return commentService.unloveComment(publishId);
    }

    /**
     * 接口名称：查询单条动态
     * 接口路径：GET/movements/:id
     */
    @GetMapping("{id}")
    public ResponseEntity<Object> queryMovementsById(@PathVariable("id") String publishId) {
        log.info("接口名称：查询单条动态");
        System.out.println("publishId = " + publishId);
//        //这是前端bug，会返回visitors给我
//        if (publishId.equals("visitors")) {
//            return null;
//        }
        return commentService.queryMovementsById(publishId);
    }

    /**
     * 接口名称：谁看过我
     * 接口路径：GET/movements/visitors
     */
    @GetMapping("/visitors")
    public ResponseEntity<Object> queryVisitors() {
        log.info("接口名称：谁看过我");
        return movementsService.queryVisitors();
    }
}
