package com.tanhua.server.controller;

import com.tanhua.domain.db.Settings;
import com.tanhua.server.service.SettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    /**
     * 用户通用设置 - 通知设置的读取
     * 接口路径：GET/users/settings
     */
    @GetMapping("/settings")
    public ResponseEntity<Object> settings() {
        log.info("接口名称：通知设置 - 读取");
        return settingsService.querySettings();
    }

    /**
     * 接口名称：通知设置 - 保存
     * 接口路径：POST/users/notifications/setting
     * 需求描述：保存或修改通知设置。如果根据用户id查询通知已经存在则修改，否则添加
     */
    @PostMapping("/notifications/setting")
    public ResponseEntity<Object> saveNotification(@RequestBody Settings settings){
        log.info("接口名称：通知设置 - 保存");
        return settingsService.saveNotification(settings);
    }

    /**
     * 接口名称：设置陌生人问题 - 保存
     * 接口路径：POST/users/questions
     * 需求描述：保存或修改陌生人问题
     */
    @PostMapping("/questions")
    public ResponseEntity<Object> saveQuestion(@RequestBody Map<String,String> paramMap){
        log.info("接口名称：设置陌生人问题 - 保存");
        String content = paramMap.get("content");
        return settingsService.saveQuestion(content);
    }

    /**
     * 接口名称：黑名单 - 翻页列表
     * 接口路径：GET/users/blacklist
     * 需求描述：分页查询黑名单列表
     */
    @GetMapping("/blacklist")
    public ResponseEntity<Object> blacklist(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer pagesize){
        return settingsService.blacklist(page,pagesize);
    }

    /**
     * 接口名称：黑名单 - 移除
     * 接口路径：DELETE/users/blacklist/:uid
     */
    @DeleteMapping("/blacklist/{uid}") //路径传值
    public ResponseEntity<Object> deleteBlackUser(@PathVariable("uid") Long blackUserId) {
        return settingsService.deleteBlackUser(blackUserId);
    }

    /**
     * 接口名称：修改手机号 - 发送验证码
     * 接口路径：POST /users/phone/sendVerificationCode
     */
    @PostMapping("/phone/sendVerificationCode")
    public ResponseEntity<Object> sendVerificationCode() {
        log.info("接口名称：修改手机号 - 发送验证码");
        return settingsService.sendVerificationCode();
    }

    /**
     * 接口名称：修改手机号 - 校验验证码
     * 接口路径：POST /users/phone/checkVerificationCode
     */
    @PostMapping("phone/checkVerificationCode")
    public ResponseEntity<Object> checkVerificationCode(@RequestBody Map<String,String> map) {
        log.info("接口名称：修改手机号 - 校验验证码");

        String verificationCode = map.get("verificationCode");
        return settingsService.checkVerificationCode(verificationCode);
    }

    /**
     * 接口名称：修改手机号 - 保存
     * 接口路径：POST /users/phone
     */
    @PostMapping("/phone")
    public ResponseEntity<Object> saveNewPhone(@RequestBody Map<String,String> map) {
        log.info("接口名称：修改手机号 - 保存");
        String phone = map.get("phone");
        return settingsService.saveNewPhone(phone);
    }
}
