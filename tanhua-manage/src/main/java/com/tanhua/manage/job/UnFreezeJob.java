package com.tanhua.manage.job;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.FreezeUser;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.FreezeApi;
import com.tanhua.manage.service.AnalysisByDayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class UnFreezeJob {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Reference
    private FreezeApi freezeApi;
    @Reference
    private UserInfoApi userInfoApi;
    String FREEZE_KEY = "FREEZE_KEY_";

    /**
     * 定时任务，每5分钟执行一次查询冻结用户，把冻结完成的用户状态设为正常
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void unFreezeUsers() {
        log.info("执行自动解封查询：--------->");
        List<FreezeUser> freezeUsers = freezeApi.find();
        for (FreezeUser freezeUser : freezeUsers) {
            if (!redisTemplate.hasKey(FREEZE_KEY + freezeUser.getUserId())) {
                freezeUser.setState(2);
                freezeUser.setReasonsForThawing("系统自动解封");
                freezeApi.unfreeze(freezeUser);
                UserInfo userInfo = userInfoApi.findById(freezeUser.getUserId().longValue());
                Integer state = 1;
                userInfo.setUserStatus(state);
                userInfoApi.update(userInfo);
            }
        }
        log.info("执行自动解封完成：--------->");
    }

}