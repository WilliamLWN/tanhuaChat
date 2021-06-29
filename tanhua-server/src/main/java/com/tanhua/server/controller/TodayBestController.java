package com.tanhua.server.controller;

import com.tanhua.domain.vo.RecommendQueryVo;
import com.tanhua.server.service.TodayBestService;
import com.tanhua.server.service.IMService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/tanhua")
@Slf4j
public class TodayBestController {

    @Autowired
    private TodayBestService todayBestService;
    @Autowired
    private IMService imService;

    /**
     * 接口名称：今日佳人，查询推荐缘分值最大的用户
     * 接口路径：GET/tanhua/todayBest
     */
    @GetMapping("/todayBest")
    public ResponseEntity<Object> todayBest(){
        log.info("接口名称：今日佳人，查询推荐缘分值最大的用户");
        return todayBestService.queryTodayBest();
    }

    /**
     * 接口名称：推荐朋友 (首页推荐)
     * 接口路径：GET/tanhua/recommendation
     */
    @GetMapping("/recommendation")
    public ResponseEntity<Object> recommendation(RecommendQueryVo vo){
        log.info("接口名称：推荐朋友 (首页推荐)");
        return todayBestService.queryRecommendation(vo);
    }

    /**
     * 接口名称：佳人信息
     * 接口路径：GET/tanhua/:id/personalInfo
     */
    @GetMapping("/{id}/personalInfo")
    public ResponseEntity<Object> queryPersonalInfo(@PathVariable("id") Long recommendUserId){
        log.info("接口名称：佳人信息");
        return todayBestService.queryPersonalInfo(recommendUserId);
    }

    /**
     * 接口名称：查询陌生人问题
     * 接口路径：GET/tanhua/strangerQuestions
     */
    @GetMapping("/strangerQuestions")
    public ResponseEntity<Object> strangerQuestions(Long userId) {
        log.info("接口名称：查询陌生人问题");
        return todayBestService.strangerQuestions(userId);
    }

    /**
     * 接口名称：回复陌生人问题
     * 接口路径：POST/tanhua/strangerQuestions
     */
    @PostMapping("/strangerQuestions")
    public ResponseEntity<Object> replyQuestions(@RequestBody Map<String,Object> paramMap) {
        log.info("接口名称：回复陌生人问题");
        Integer strangerId = (Integer) paramMap.get("userId");
        String reply = (String) paramMap.get("reply");
        return imService.replyQuestions(strangerId,reply);
    }

    /**
     * 接口名称：搜附近
     * 接口路径：GET/tanhua/search
     */
    @GetMapping("/search")
    public ResponseEntity<Object> searchNear(String gender,Long distance){
        log.info("接口名称：搜附近");
        System.out.println("gender = " + gender);
        System.out.println("distance = " + distance);
        return todayBestService.searchNear(gender,distance);
    }

    /**
     * 接口名称：探花-左滑右滑
     * 接口路径：GET/tanhua/cards
     */
    @GetMapping("/cards")
    public ResponseEntity<Object> cards(){
        log.info("接口名称：探花-左滑右滑");
        return todayBestService.cards();
    }

    /**
     * 接口名称：探花-不喜欢
     * 接口路径：GET/tanhua/:id/unlove
     */
    @GetMapping("/{id}/unlove")
    public ResponseEntity<Object> unlove(@PathVariable("id") Long likeUserId){
        log.info("接口名称：探花-不喜欢");
        return todayBestService.unlove(likeUserId);
    }

    /**
     * 接口名称：探花-喜欢
     * 接口路径：GET/tanhua/:id/love
     */
    @GetMapping("/{id}/love")
    public ResponseEntity<Object> love(@PathVariable("id") Long likeUserId){
        log.info("接口名称：探花-喜欢");
        return todayBestService.love(likeUserId);
    }
}
