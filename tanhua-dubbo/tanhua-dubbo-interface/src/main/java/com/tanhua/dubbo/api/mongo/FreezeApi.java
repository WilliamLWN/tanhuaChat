package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.FreezeUser;

import java.util.List;

public interface FreezeApi {
    void save(FreezeUser freezeUser);

    List<FreezeUser> find();

    void unfreeze(FreezeUser freezeUser);

    FreezeUser findByUserId(Integer userId);
}
