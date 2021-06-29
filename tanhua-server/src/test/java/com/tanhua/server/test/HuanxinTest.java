package com.tanhua.server.test;

import com.alibaba.fastjson.JSON;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class HuanxinTest {
    /**
     * 获取管理员权限
     * 参考环信官网服务端集成，用户体系集成，生成token信息；
     * 为什么要生成token？
     * 1、生成token
     * 2、项目要连上环信，需要携带token参数
     */
    public static void main(String[] args) {

        // 创建远程访问的工具类，支持rest请求
        RestTemplate restTemplate = new RestTemplate();

        // 参数1：远程访问的链接地址
        String url = "http://a1.easemob.com/1109210512148046/tanhua/token";
        // 参数2：创建一个map对象，封装请求的数据（参考官网）
        Map<String,String> map = new HashMap<>();
        map.put("grant_type","client_credentials");
        map.put("client_id","YXA6-SEYRIT7TJ-6I9I8jxi4YA");
        map.put("client_secret","YXA6lGM-E-dkCKgtyHtOfy41GKB25ac");
        // 对象转json
        String jsonString = JSON.toJSONString(map);
        ResponseEntity<String> entity = restTemplate.postForEntity(url, jsonString, String.class);

        // 获取响应的数据 {"access_token":"","application":"expires_in":5184000}
        String body = entity.getBody();
        System.out.println("响应的数据 = " + body);

        // 只获取token
        Map<String,String> resultMap = JSON.parseObject(body, Map.class);
        System.out.println("最终获取到token = " + resultMap.get("access_token"));
    }
}
