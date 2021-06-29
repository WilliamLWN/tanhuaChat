package com.tanhua.server.test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JwtTest {
    public static void main(String[] args) {
        //1.定义要加密的数据
        Map<String,Object> map = new HashMap<>();
        map.put("id", 1);
        map.put("mobile", "13302762379");

        //2.定义密钥
        String secret = "tanhua";

        //3.生成token
        String token = Jwts.builder()
                .setClaims(map)  //需要加密的数据
                .signWith(SignatureAlgorithm.HS256, secret)  //指定加密算法和密钥
                .compact();

        System.out.println("token = " + token);

        // 4、根据token，解析数据，获取map集合
        Map<String, Object> body = (Map<String, Object>) Jwts.parser().setSigningKey(secret).parse(token).getBody();

        System.out.println("body = " + body);
    }
}
