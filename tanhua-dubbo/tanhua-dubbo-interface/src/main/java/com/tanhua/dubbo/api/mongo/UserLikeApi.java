package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.UserLike;
import com.tanhua.domain.vo.PageResult;

public interface UserLikeApi {
    Long queryEachLoveCount(Long userId);

    Long queryLoveCount(Long userId);

    Long queryFanCount(Long userId);

    PageResult queryEachLoveList(Long userId, Integer page, Integer pagesize);

    PageResult queryLoveList(Long userId, Integer page, Integer pagesize);

    PageResult queryFanList(Long userId, Integer page, Integer pagesize);

    PageResult queryVisitorList(Long userId, Integer page, Integer pagesize);

    void delete(Long likeUserId, Long userId);

    void save(Long userId, Long unlikeUserId);

    void saveLike(Long userId, Long likeUserId);

    UserLike findMyLike(Long userId, Long likeUserId);

    void deleteLike(Long userId, Long likeUserId);
}
