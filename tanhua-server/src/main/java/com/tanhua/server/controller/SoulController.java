package com.tanhua.server.controller;

import com.tanhua.domain.vo.Answers;
import com.tanhua.server.service.SoulService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/testSoul")
public class SoulController {
    @Autowired
    private SoulService soulService;

    /**
     * 接口名称：测灵魂-问卷列表
     * 接口路径：GET/testSoul
     */
    @GetMapping
    public ResponseEntity<Object> testSoul() {
        log.info("接口名称：测灵魂-问卷列表");
        return soulService.testSoul();
    }

    /**
     * 接口名称：测灵魂-提交问卷
     * 接口路径：POST/testSoul
     */
    @PostMapping
    public ResponseEntity<Object> sendTestSoul(@RequestBody Answers answerr) {
        log.info("接口名称：测灵魂-提交问卷");
        return soulService.sendTestSoul(answerr);
    }

    /**
     * 接口名称：测灵魂-查看结果
     * 接口路径：GET/testSoul/report/:id
     */
    @GetMapping("/report/{id}")
    public ResponseEntity<Object> report(@PathVariable("id") String reportId) {
        log.info("接口名称：测灵魂-查看结果");
        return soulService.report(reportId);
    }
}
