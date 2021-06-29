package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.db.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;

import java.util.List;

public interface VideoApi {
    PageResult findByPage(Integer page, Integer pagesize);

    void save(Video video);

    void followUser(FollowUser followUser);

    void unfollowUser(Long userId, Long followUserId);

    PageResult findByPage(Integer page, Integer pagesize, Long uid);

    Video findById(String videoId);

    List<Video> queryVideoListByPids(List<Long> vidList);
}
