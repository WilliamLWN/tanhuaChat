package com.tanhua.manage.service;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tanhua.manage.domain.Admin;
import com.tanhua.manage.exception.BusinessException;
import com.tanhua.manage.mapper.AdminMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService extends ServiceImpl<AdminMapper, Admin> {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Value("tanhua.secret")
    private String secret;

    /**
     * 验证码存入redis
     * @param uuid 存储redis中的key
     * @param code 验证码
     */
    public void saveCap(String uuid, String code) {
        String key = "MANAGE_CAP_"+uuid;
        redisTemplate.opsForValue().set(key,code, Duration.ofMinutes(2));
    }

    /**
     * 接口名称：用户登录
     * 通过mybatis-plus提供的ServiceImpl实现类，封装了基础的crud、分页查询操作
     */
    public ResponseEntity<Object> login(Map<String, String> map) {
        //1. 获取请求参数
        String username = map.get("username");
        String password = map.get("password");
        String verificationCode = map.get("verificationCode");
        String uuid = map.get("uuid");

        //2. 判断：用户名密码
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new BusinessException("用户名或密码为空！");
        }
        //3. 判断：验证码
        //3.1 非空判断
        if (StringUtils.isEmpty(verificationCode)) {
            throw new BusinessException("验证码为空！");
        }
        //3.2 校验
        String key = "MANAGE_CAP_"+uuid;
        String redisCode = redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(redisCode) || !redisCode.equals(verificationCode)) {
            throw new BusinessException("验证码校验失败！");
        }

        //4. 删除验证码
        redisTemplate.delete(key);
        //5. 根据用户名查询并判断用户名是否存在
        Admin admin = query().eq("username", username).one();
        if (admin == null) {
            throw new BusinessException("用户名不存在！");
        }
        //6. 判断密码
        if (!admin.getPassword().equals(SecureUtil.md5(password))){
            throw new BusinessException("密码错误！");
        }
        //7. 生成token
        Map<String,Object> claimsMap = new HashMap<>();
        claimsMap.put("id",admin.getId());
        claimsMap.put("username",admin.getUsername());
        String token = Jwts.builder().setClaims(claimsMap)
                .signWith(SignatureAlgorithm.HS256, secret).compact();

        //8. 用户对象存入redis
        String adminStr = JSON.toJSONString(admin);
        redisTemplate.opsForValue().set("MANAGE_TOKEN_"+token,adminStr,Duration.ofHours(4));

        //9. 返回
        Map<String,String> result = new HashMap<>();
        result.put("token",token);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据token获取admin对象
     * @param token
     * @return
     */
    public Admin findUserByToken(String token) {
        String key = "MANAGE_TOKEN_"+token;
        String adminStr = redisTemplate.opsForValue().get(key);
        return JSON.parseObject(adminStr,Admin.class);
    }

    /**
     * 接口名称：用户登出
     */
    public ResponseEntity<Object> logout(String token) {
        redisTemplate.delete("MANAGE_TOKEN_"+token);
        return ResponseEntity.ok(null);
    }
}
