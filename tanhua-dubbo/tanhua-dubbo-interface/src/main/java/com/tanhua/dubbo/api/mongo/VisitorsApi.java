package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Visitors;

import java.util.List;

public interface VisitorsApi {

    /**
     * 显示最近5个访客
     */
    List<Visitors> queryVisitors(Long userId, int top);

    /**
     * 查看最近的访客
     */
    List<Visitors> queryVisitors(Long userId, Long time);
}
