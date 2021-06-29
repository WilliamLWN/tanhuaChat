package com.tanhua.server.controller;

import com.tanhua.domain.db.User;
import com.tanhua.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping("/findByMobile")
    public ResponseEntity<Object> findByMobile(String mobile){
        return userService.findByMobile(mobile);
    }

    @PostMapping
    public ResponseEntity<Object> save(@RequestBody User user){
        return userService.save(user);
    }

    /**
     * 接口名称：登录第一步---手机号登录
     * 接口路径：POST/user/login
     * 需求描述：根据手机号码，发送验证码
     */
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Map<String,String> paramMap){
        log.info("接口名称：登录第一步---手机号登录");
        String phone = paramMap.get("phone");
        return userService.sendSms(phone);
    }

    /**
     * 接口名称：登录第二步---验证码校验
     * 接口路径：POST/user/loginVerification
     * 需求分析：根据用户输入的验证码，获取redis中验证码校验。
     */
    @PostMapping("/loginVerification")
    public ResponseEntity<Object> loginVerification(
            @RequestBody Map<String,String> paramMap){
        log.info("登录第二步---验证码校验");
        String phone = paramMap.get("phone");
        String verificationCode = paramMap.get("verificationCode");
        return userService.loginVerification(phone,verificationCode);
    }
}