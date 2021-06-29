package com.tanhua.dubbo.api;

import com.tanhua.domain.db.User;

public interface UserApi {

    /**
     * 保存用户
     * @param user
     * @return 返回自增长的主键值
     */
    Long save(User user);

    /**
     * 根据手机号查询
     * @param mobile 要查询的手机号码
     * @return 返回查询的用户
     */
    User findByMobile(String mobile);

    void update(User user);
}
