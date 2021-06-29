package com.tanhua.server.controller;

import com.tanhua.server.service.IMService;
import com.tanhua.server.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/messages")
@Slf4j
public class IMController {

    @Autowired
    private MessageService messageService;
    @Autowired
    private IMService imService;

    /**
     * 接口名称：公告列表 -- 显示
     * 接口路径：GET/messages/announcements
     * 需求描述：根据手机号码，发送验证码
     */
    @GetMapping("/announcements")
    public ResponseEntity<Object> announcements(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer pagesize){

        System.out.println("page = " + page);
        System.out.println("pagesize = " + pagesize);

        log.info("接口名称：公告列表 -- 显示");
        return messageService.announcements(page,pagesize);
    }

    /**
     * 接口名称：联系人添加
     * 接口路径：POST/messages/contacts
     */
    @PostMapping("/contacts")
    public ResponseEntity<Object> addContract(@RequestBody Map<String, Integer> paramMap) {
        log.info("接口名称：联系人添加");
        Integer userId = paramMap.get("userId");
        return imService.addContract(userId.longValue());
    }

    /**
     * 接口名称：联系人列表
     * 接口路径：GET/messages/contacts
     * 需求描述：分页查询当前用户的好友列表
     */
    @GetMapping("/contacts")
    public ResponseEntity<Object> queryContractList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pagesize, String keyword) {
        log.info("接口名称：联系人列表");
        return imService.queryContractList(page, pagesize, keyword);
    }

    /**
     * 接口名称：点赞列表
     * 接口路径：GET/messages/likes
     * 需求描述：分页查询当前用户的点赞列表
     */
    @GetMapping("/likes")
    public ResponseEntity<Object> queryLikesList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pagesize) {
        log.info("接口名称：点赞列表");
        return imService.queryLikesList(page, pagesize);
    }

    /**
     * 接口名称：评论列表
     * 接口路径：GET/messages/comments
     * 需求描述：分页查询当前用户的评论列表
     */
    @GetMapping("/comments")
    public ResponseEntity<Object> queryCommentsList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pagesize) {
        log.info("接口名称：评论列表");
        return imService.queryCommentsList(page, pagesize);
    }

    /**
     * 接口名称：喜欢列表
     * 接口路径：GET/messages/loves
     * 需求描述：分页查询当前用户的评论列表
     */
    @GetMapping("/loves")
    public ResponseEntity<Object> queryLovesList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pagesize) {
        log.info("接口名称：喜欢列表");
        return imService.queryLovesList(page, pagesize);
    }
}
