package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.vo.PageResult;

public interface FriendApi {
    void save(Long userId, Long friendId);

    PageResult findFriendByUserId(Integer page, Integer pagesize, String keyword, Long userId);

    void delete(Long unlikeUserId, Long userId);
}
