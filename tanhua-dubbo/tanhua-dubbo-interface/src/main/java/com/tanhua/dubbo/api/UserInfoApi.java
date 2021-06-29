package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.domain.db.UserInfo;

import java.util.List;

public interface UserInfoApi {
    /**
     * 保存用户详情
     */
    void save(UserInfo userInfo);

    /**
     * 更新用户详情
     * @param userInfo
     */
    void update(UserInfo userInfo);

    UserInfo findById(Long userId);

    IPage<UserInfo> findByPage(Integer page, Integer pagesize);

    boolean freezeUserStatus(Integer userId);

    List<UserInfo> findAll(Long userId);
}
