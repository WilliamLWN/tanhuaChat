package com.tanhua.dubbo.api.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.User;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.dubbo.mapper.UserMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Service
 * 1、修饰dubbo服务类，给消费者远程调用
 * 2、导入dubbo提供的注解：org.apache.dubbo.config.annotation.Service
 * 3、不要导入spring的包：org.springframework.stereotype.Service
 */
@Service
public class UserApiImpl implements UserApi {

    @Autowired
    private  UserMapper userMapper;

    @Override
    public Long save(User user) {
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public User findByMobile(String phone) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile", phone);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public void update(User user) {
        userMapper.updateById(user);
    }
}