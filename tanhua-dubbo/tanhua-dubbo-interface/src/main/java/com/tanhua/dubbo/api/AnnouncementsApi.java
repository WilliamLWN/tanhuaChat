package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.domain.db.Announcement;

public interface AnnouncementsApi {

    IPage<Announcement> findAnnouncementsList(Integer page, Integer pagesize);
}
