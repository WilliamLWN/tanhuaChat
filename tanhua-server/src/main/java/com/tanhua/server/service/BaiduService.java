package com.tanhua.server.service;

import com.tanhua.dubbo.api.mongo.UserLocationApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class BaiduService {
    @Reference
    private UserLocationApi userLocationApi;

    /**
     * 接口名称：上报地理信息
     */
    public ResponseEntity<Object> saveLocation(Double latitude, Double longitude, String addrStr) {
        userLocationApi.saveLocation(latitude,longitude,addrStr, UserHolder.getUserId());
        return ResponseEntity.ok(null);
    }
}
