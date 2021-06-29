package com.tanhua.dubbo.api.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.Announcement;
import com.tanhua.dubbo.api.AnnouncementsApi;
import com.tanhua.dubbo.mapper.AnnouncementsMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class AnnouncementsApiImpl implements AnnouncementsApi {

    @Autowired
    private AnnouncementsMapper announcementsMapper;

    @Override
    public IPage<Announcement> findAnnouncementsList(Integer page, Integer pagesize) {

        IPage<Announcement> pageParam = new Page<>(page, pagesize);
        QueryWrapper<Announcement> querywrapper = new QueryWrapper<>();

        return announcementsMapper.selectPage(pageParam, querywrapper);
    }
}
