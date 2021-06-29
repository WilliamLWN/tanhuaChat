package com.tanhua.server.controller;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 修改用户详细信息和回显
 */
@RestController
@RequestMapping("/users")
@Slf4j
public class UserInfoController {

    @Autowired
    private UserService userService;

    /**
     * 接口名称：用户资料 - 读取
     * 接口路径：GET/users
     * 需求描述：
     *   1.根据用户id查询用户详情
     *   2.如果userID不为NULL，根据用户id查询
     *   3.如果用户id为空，huanxinID不为NULL，根据环信ID查询
     *   4.如果userID与huanxinID都为空，从token获取用户id
     */
    @GetMapping
    public ResponseEntity<Object> findUserInfoById(Long userID,Long huanxinID) throws IOException {
        log.info("接口名称：用户资料 - 读取");
        return userService.findUserInfoById(userID, huanxinID);
    }

    /**
     * 接口名称：用户资料 - 保存
     * 接口路径：PUT/users
     */
    @PutMapping
    public ResponseEntity<Object> updateUserInfo(@RequestBody UserInfo userInfo) {
        log.info("接口名称：用户资料 - 保存");
        return userService.updateUserInfo(userInfo);
    }

    /**
     * 接口名称：用户资料 - 更新头像
     * 接口路径：POST/users/header
     */
    @PostMapping("/header")
    public ResponseEntity<Object> updateUserHead(MultipartFile headPhoto) throws IOException {
        log.info("接口名称：用户资料 - 更新头像");
        return userService.updateUserHead(headPhoto);
    }

    /**
     * 接口名称：互相喜欢，喜欢，粉丝 - 统计
     * 接口路径：GET/users/counts
     */
    @GetMapping("/counts")
    public ResponseEntity<Object> queryCounts(){
        log.info("接口名称：互相喜欢，喜欢，粉丝 - 统计");
        return userService.queryCounts();
    }

    /**
     * 接口名称：互相喜欢、喜欢、粉丝、谁看过我  (列表)
     * 接口路径：GET/users/friends/:type
     */
    @GetMapping("/friends/{type}")
    public ResponseEntity<Object> queryUserLikeList(
            @PathVariable("type") Integer type,
            @RequestParam(defaultValue = "1")Integer page,
            @RequestParam(defaultValue = "10")Integer pagesize){
        log.info("接口名称：互相喜欢、喜欢、粉丝、谁看过我  (列表)");
        return userService.queryUserLikeList(type,page,pagesize);
    }

    /**
     * 接口名称：粉丝 - 喜欢
     * 接口路径：POST/users/fans/:uid
     */
    @PostMapping("/fans/{uid}")
    public ResponseEntity<Object> fansLike(@PathVariable("uid") Long likeUserId){
        log.info("接口名称：粉丝 - 喜欢");
        return userService.fansLike(likeUserId);
    }

    /**
     * 接口名称：粉丝 - 取消喜欢
     * 接口路径：DELETE/users/like/:uid
     */
    @DeleteMapping("/like/{uid}")
    public ResponseEntity<Object> removeLike(@PathVariable("uid") Long unlikeUserId){
        log.info("接口名称：粉丝 - 取消喜欢");
        return userService.removeLike(unlikeUserId);
    }

}
