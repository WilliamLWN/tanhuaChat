package com.tanhua.manage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
@MapperScan("com.tanhua.manage.mapper") // 扫描mapper接口
@EnableScheduling        // 开启定时任务支持,用于rocketMQ定时收发信息
public class ManagerServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManagerServerApplication.class, args);
    }
}
