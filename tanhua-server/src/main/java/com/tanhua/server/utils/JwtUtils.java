package com.tanhua.server.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.HashMap;
import java.util.Map;

public class JwtUtils {

    public static String createToken(Long id,String mobile,String secret ) {
        // 定义要加密的数据
        Map<String,Object> map = new HashMap<>();
        map.put("id",id);
        map.put("mobile",mobile);

        // 生成token并返回
        return Jwts.builder()
                .setClaims(map) // 声明加密的数据
                .signWith(SignatureAlgorithm.HS256, secret) // 指定加密算法与密钥
                .compact();
    }
}
