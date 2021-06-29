package com.tanhua.server.controller;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 接口名称：新用户---1填写资料
     * 接口路径：POST/user/loginReginfo
     * 需求描述：用户第一次登陆，需要完善用户信息
     */
    @PostMapping("/loginReginfo")
    public ResponseEntity<Object> loginReginfo(
            @RequestBody UserInfo userInfo){
        log.info("新用户---1填写资料");
        return userService.saveUserInfo(userInfo);
    }

    /**
     * 完善用户信息 - 上传用户头像（修改UserInfo中头像字段）
     */
    @PostMapping("/loginReginfo/head")
    public ResponseEntity<Object> setUserHead(
            MultipartFile headPhoto) throws IOException {
        return userService.setUserHead(headPhoto);
    }
}
