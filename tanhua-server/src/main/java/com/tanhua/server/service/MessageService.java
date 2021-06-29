package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.domain.db.Announcement;
import com.tanhua.domain.vo.AnnouncementVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.AnnouncementsApi;
import com.tanhua.server.utils.RelativeDateFormat;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {

    @Reference
    private AnnouncementsApi announcementsApi;

    /**
     * 接口名称：公告列表 -- 显示
     */
    public ResponseEntity<Object> announcements(Integer page, Integer pagesize) {
        //1. 调用Api服务分页查询
        IPage<Announcement> iPage = announcementsApi.findAnnouncementsList(page, pagesize);
        List<Announcement> announcements = iPage.getRecords();
        //2. 封装返回结果：PageResult
        List<AnnouncementVo> announcementVoList = new ArrayList<>();

        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Announcement announcement : announcements) {
            AnnouncementVo announcementVo = new AnnouncementVo(announcement.getId().toString(),announcement.getTitle(),announcement.getDescription(), RelativeDateFormat.format(announcement.getCreated()));
            announcementVoList.add(announcementVo);
        }
        System.out.println("announcements = " + announcements);
        System.out.println("announcementVoList = " + announcementVoList);
        PageResult pageResult = new PageResult(page,pagesize, (int) iPage.getTotal(), announcementVoList);
        System.out.println("pageResult = " + pageResult);
        return ResponseEntity.ok(pageResult);
    }
}
