package com.tanhua.dubbo.api.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.BlackList;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.dubbo.api.BlackListApi;
import com.tanhua.dubbo.mapper.BlackListMapper;
import com.tanhua.dubbo.mapper.UserInfoMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class blackListApiImpl implements BlackListApi {

    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private BlackListMapper blackListMapper;

    @Override
    public IPage<UserInfo> findBlackList(Integer page, Integer pagesize, Long userId) {

        Page<UserInfo> pageParam = new Page<>(page, pagesize);
        return userInfoMapper.findBlackList(pageParam, userId);
    }

    @Override
    public void deleteBlackUser(Long userId, Long blackUserId) {

        QueryWrapper<BlackList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_Id", userId);
        queryWrapper.eq("black_user_Id", blackUserId);
        blackListMapper.delete(queryWrapper);

        //第二种方法，在mapper接口写方法
//        blackListMapper.deleteBlackUser(userId,blackUserId);
    }
}
