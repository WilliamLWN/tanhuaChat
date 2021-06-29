package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.domain.db.UserInfo;

public interface BlackListApi {
    IPage<UserInfo> findBlackList(Integer page, Integer pagesize, Long userId);

    void deleteBlackUser(Long userId, Long blackUserId);
}
