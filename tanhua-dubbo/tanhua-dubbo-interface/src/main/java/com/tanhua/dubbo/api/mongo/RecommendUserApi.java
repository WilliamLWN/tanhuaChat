package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;

public interface RecommendUserApi {
    RecommendUser queryWithMaxScore(Long userId);

    PageResult queryRecommendation(Long userId, Integer page, Integer pagesize);

    long queryScore(Long userId, Long recommendUserId);
}
