package com.tanhua.server.controller;

import com.tanhua.server.service.CommentService;
import com.tanhua.server.service.MovementsMQService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("comments")
@Slf4j
public class CommentsController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private MovementsMQService movementsMQService;

    /**
     * 接口名称：评论列表
     * 接口路径：GET/comments
     */
    @GetMapping
    public ResponseEntity<Object> queryCommentsList(
            String movementId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pagesize) {
        log.info("接口名称：评论列表");
        return commentService.queryCommentsList(movementId, page, pagesize);
    }

    /**
     * 接口名称：评论-提交
     * 接口路径：POST/comments
     */
    @PostMapping
    public ResponseEntity<Object> saveComments(@RequestBody Map<String,String> paramMap) {
        String movementId = paramMap.get("movementId");
        String commentText = paramMap.get("comment");
        // [发送mq消息，用于推荐计算]
        movementsMQService.commentPublishMsg(movementId);
        return commentService.saveComments(movementId,commentText);
    }

}
