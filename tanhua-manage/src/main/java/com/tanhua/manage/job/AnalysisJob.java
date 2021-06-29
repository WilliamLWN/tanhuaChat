package com.tanhua.manage.job;

import com.tanhua.manage.service.AnalysisByDayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Slf4j
public class AnalysisJob {

    @Autowired
    private AnalysisByDayService analysisByDayService;

//    @Scheduled(cron = "0/5 * * * * ?")  //每5秒打印当前系统时间
//    public void analysis() {
//        System.out.println("当前时间："+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//    }

    /**
     * 定时任务，每5分钟执行一次数据统计，把log的数据统计到AnalysisByDay表中
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void analysis(){
        log.info("执行统计开始：--------->");
        analysisByDayService.anasysis();
        log.info("执行统计结束：--------->");
    }

}
