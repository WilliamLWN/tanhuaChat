package com.tanhua.dubbo.api.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.mapper.UserInfoMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserInfoApiImpl implements UserInfoApi {
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public void save(UserInfo userInfo) {
        userInfoMapper.insert(userInfo);
    }

    @Override
    public void update(UserInfo userInfo) {
        userInfoMapper.updateById(userInfo);
    }

    @Override
    public UserInfo findById(Long userId) {
        return userInfoMapper.selectById(userId);
    }

    @Override
    public IPage<UserInfo> findByPage(Integer page, Integer pagesize) {
        IPage iPage = new Page(page,pagesize);
        return userInfoMapper.selectPage(iPage,null);
    }

    @Override
    public boolean freezeUserStatus(Integer userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        userInfo.setUserStatus(2);
        userInfoMapper.updateById(userInfo);
        return true;
    }

    @Override
    public List<UserInfo> findAll(Long userId) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("id", userId);
        return userInfoMapper.selectList(queryWrapper);
    }
}
