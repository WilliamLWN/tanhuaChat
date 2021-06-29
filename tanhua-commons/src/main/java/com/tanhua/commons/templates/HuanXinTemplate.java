package com.tanhua.commons.templates;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.properties.HuanXinProperties;
import com.tanhua.domain.vo.HuanXinUser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;

public class HuanXinTemplate {

    private HuanXinProperties properties;
    private RestTemplate restTemplate ;
    private String token;
    private long expire = 0L;

    public HuanXinTemplate(HuanXinProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    /**
     * 注册环信用户
     * 根据环信的token注册环信用户
     */
    public void register(Long userId) {

        String url = properties.getHuanXinUrl() + "/users";

        String token = getToken();

        //请求头信息
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Authorization", "Bearer " + token);

        List<HuanXinUser> huanXinUsers = new ArrayList<>();
        huanXinUsers.add(new HuanXinUser(userId.toString(), "123456"));

        try {
            HttpEntity<String> httpEntity = new HttpEntity(JSON.toJSONString(huanXinUsers), httpHeaders);
            //发起请求
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加好友
     * 根据环信token添加好友
     */
    public void contactUsers(Long userId, Long friendId) {

        String targetUrl = properties.getHuanXinUrl()+ "/users/" + userId + "/contacts/users/" + friendId;

        // 请求头
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json ");
        headers.add("Authorization", "Bearer " + getToken());

        try {
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(targetUrl, httpEntity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送消息
     * 根据环信token发送消息
     */
    public void sendMsg(String target, String msg) {

        String targetUrl = properties.getHuanXinUrl() + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json ");
        headers.add("Authorization", "Bearer " + getToken());

        try {
            //请求头
            Map<String, Object> requestParam = new HashMap<>();
            requestParam.put("target_type", "users");
            requestParam.put("target", Arrays.asList(target));

            Map<String, Object> msgParam = new HashMap<>();
            msgParam.put("msg", msg);
            msgParam.put("type", "txt");

            requestParam.put("msg", msgParam);

            //表示消息发送者;无此字段Server会默认设置为“from”:“admin”，有from字段但值为空串(“”)时请求失败
            //requestParam.put("from", null);

            HttpEntity<Map> httpEntity = new HttpEntity<>(requestParam, headers);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(targetUrl, httpEntity, String.class);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //获取环信的token
    private String getToken() {

        Long now = System.currentTimeMillis();

        if(now > expire) {

            String url = this.properties.getUrl() + this.properties.getOrgName() + "/" + this.properties.getAppName() + "/token";

            Map<String, Object> param = new HashMap<>();

            param.put("grant_type", "client_credentials");
            param.put("client_id", this.properties.getClientId());
            param.put("client_secret", this.properties.getClientSecret());

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, param, String.class);

            String body = responseEntity.getBody();

            Map<String,String> map1 = JSON.parseObject(body, Map.class);

            token = map1.get("access_token");

            expire = System.currentTimeMillis() + 24 * 60 * 60 * 1000;
        }
        return token;
    }


}
