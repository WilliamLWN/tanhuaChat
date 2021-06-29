package com.tanhua.manage.controller;

import com.tanhua.manage.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manage")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 接口名称：用户数据翻页
     * 接口路径：GET/manage/users
     */
    @GetMapping("/users")
    public ResponseEntity<Object> findByPage(Integer page, Integer pagesize) {
        log.info("接口名称：用户数据翻页");
        return userService.findByPage(page, pagesize);
    }

    /**
     * 接口名称：用户基本资料
     * 接口路径：GET/manage/users/:userID
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<Object> findById(@PathVariable("userId") Long userId) {
        log.info("接口名称：用户基本资料");
        return userService.findById(userId);
    }

    /**
     * 接口名称：用户冻结操作
     * 接口路径：POST/manage/users/freeze
     */
    @PostMapping("/users/freeze")
    public ResponseEntity<Object> freeze(@RequestBody Map<String, String> param) {
        log.info("接口名称：用户冻结操作");
        return userService.freeze(param);
    }

    /**
     * 接口名称：用户解冻操作
     * 接口路径：POST/manage/users/unfreeze
     */
    @PostMapping("/users/unfreeze")
    public ResponseEntity<Object> unfreeze(@RequestBody Map<String, String> param) {
        log.info("接口名称：用户解冻操作");
        return userService.unfreeze(param);
    }

    /**
     * 接口名称：视频记录翻页
     * 接口路径：GET/manage/videos
     */
    @GetMapping("/videos")
    public ResponseEntity<Object> findVideosList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pagesize, Long uid) {
        log.info("接口名称：视频记录翻页");
        return userService.findVideosList(page, pagesize, uid);
    }

    /**
     * 接口名称：动态分页
     * 接口路径：GET/manage/messages
     */
    @GetMapping("/messages")
    public ResponseEntity<Object> findMovementsList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pagesize, Long uid, String state) {
        log.info("接口名称：动态分页");
        System.out.println("uid：" + uid);
        System.out.println("state：" + state);
        Long state1 = null;
        if(state != null) {
            if (state.equals("0") || state.equals("1") || state.equals("2")) {
                state1 = Long.valueOf(state);
            } else {
                return userService.findMovementsList(page, pagesize, uid, null);
            }
        }
        return userService.findMovementsList(page, pagesize, uid, state1);
    }

    /**
     * 接口名称：动态详情
     * 接口路径：GET/manage/messages/:id
     */
    @GetMapping("/messages/{id}")
    public ResponseEntity<Object> findMovementsById(@PathVariable("id") String publishId) {
        log.info("接口名称：动态详情");
        return userService.findMovementsById(publishId);
    }

    /**
     * 接口名称：评论列表翻页
     * 接口路径：GET/manage/messages/comments
     */
    @GetMapping("/messages/comments")
    public ResponseEntity<Object> findCommentsById(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pagesize,
            @RequestParam(name = "messageID") String publishId) {
        log.info("接口名称：评论列表翻页");
        return userService.findCommentsById(publishId, page, pagesize);
    }

    /**
     * 接口名称：动态通过
     * 接口路径：POST/manage/messages/pass
     */
    @PostMapping("/messages/pass")
    public ResponseEntity<Object> passMovements(@RequestBody String[] items) {
        log.info("接口名称：动态通过");
        return userService.passMovements(items);
    }

    /**
     * 接口名称：动态拒绝
     * 接口路径：POST/manage/messages/reject
     */
    @PostMapping("/messages/reject")
    public ResponseEntity<Object> rejectMovements(@RequestBody String[] items) {
        log.info("接口名称：动态拒绝");
        return userService.rejectMovements(items);
    }
}
