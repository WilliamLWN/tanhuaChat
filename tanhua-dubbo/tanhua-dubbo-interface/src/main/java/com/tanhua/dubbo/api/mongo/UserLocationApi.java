package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.vo.UserLocationVo;

import java.util.List;

public interface UserLocationApi {
    void saveLocation(Double latitude, Double longitude, String addrStr, Long userId);

    List<UserLocationVo> searchNear(Long userId, Long distance);
}
