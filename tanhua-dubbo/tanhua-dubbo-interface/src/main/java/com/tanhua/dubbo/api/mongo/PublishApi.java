package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.vo.PageResult;

import java.util.List;

public interface PublishApi {
    void save(Publish publish);

    PageResult findByTimeLine(Integer page, Integer pagesize, Long userId);

    PageResult queryRecommendPublishList(Integer page, Integer pagesize, Long userId);

    PageResult queryMyAlbumList(Integer page, Integer pagesize, Long userId);

    Publish findById(String publishId);

    PageResult findPublishList(Integer page, Integer pagesize, Long uid, Long state);

    void updateState(String publishId, Integer state);

    List<Publish> findByPids(List<Long> pidList);
}
