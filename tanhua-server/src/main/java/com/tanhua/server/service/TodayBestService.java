package com.tanhua.server.service;

import com.tanhua.domain.db.Question;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.UserLike;
import com.tanhua.domain.vo.*;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.mongo.RecommendUserApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.UserLikeApi;
import com.tanhua.dubbo.api.mongo.UserLocationApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class TodayBestService {
    @Reference
    private UserInfoApi userInfoApi;
    @Reference
    private RecommendUserApi recommendUserApi;
    @Reference
    private QuestionApi questionApi;
    @Reference
    private UserLocationApi userLocationApi;
    @Reference
    private UserLikeApi userLikeApi;

    /**
     * 接口名称：今日佳人
     */
    public ResponseEntity<Object> queryTodayBest() {
        //1. 获取当前登陆用户
        Long userId = UserHolder.getUserId();

        //2. 根据当前登陆用户，调用Api接口，查询今日佳人
        // db.recommend_user.find({userId:20}).sort({score:-1}).limit(1)
        RecommendUser recommendUser = recommendUserApi.queryWithMaxScore(userId);
        if (recommendUser == null) {
            recommendUser = new RecommendUser();
            recommendUser.setRecommendUserId(2L);
            recommendUser.setScore(80D);
        }

        //3. 通过定义的TodayBestVo对象，封装返回的数据
        //3.1 创建返回结果
        TodayBestVo todayBestVo = new TodayBestVo();
        //3.2 根据推荐的用户id查询用户信息（今日佳人）
        UserInfo userInfo = userInfoApi.findById(recommendUser.getRecommendUserId());
        if (userInfo != null) {
            //3.3 封装用户信息
            BeanUtils.copyProperties(userInfo, todayBestVo);
            //3.4 设置tags
            if (userInfo.getTags() != null) {
                todayBestVo.setTags(userInfo.getTags().split(","));
            }
            //3.4 设置缘分值（调用Api查询的结果）
            todayBestVo.setFateValue(recommendUser.getScore().longValue());
        }
        //4. 把封装好的TodayBestVo设置到响应体中，返回给前端
        return ResponseEntity.ok(todayBestVo);
    }

    /**
     * 接口名称：推荐朋友 (首页推荐)
     */
    public ResponseEntity<Object> queryRecommendation(RecommendQueryVo vo) {
        //1. 获取当前登陆用户
        Long userId = UserHolder.getUserId();

        //2. 根据当前登陆用户，调用Api接口，分页查询推荐用户
        PageResult pageResult =
                recommendUserApi.queryRecommendation(userId,vo.getPage(),vo.getPagesize());
        //3. 获取查询到的数据
        List<RecommendUser> recommendUserList = (List<RecommendUser>) pageResult.getItems();
        if (recommendUserList == null) {
            // 如果查询为空，推荐用户默认就是id=5-10
            recommendUserList = new ArrayList<>();
            for (Integer i = 5; i < 10; i++) {
                RecommendUser ru = new RecommendUser();
                ru.setRecommendUserId(i.longValue());
                ru.setScore(70D+i);
                recommendUserList.add(ru);
            }
        }
        //4. 封装TodayBestVo集合对象
        //4.1 创建集合
        List<TodayBestVo> result = new ArrayList<>();
        //4.2 转换：把查询到的recommendUserList数据，封装到todayBestVo
        for (RecommendUser recommendUser : recommendUserList) {
            TodayBestVo todayBestVo = new TodayBestVo();
            UserInfo userInfo = userInfoApi.findById(recommendUser.getRecommendUserId());
            if (userInfo != null) {
                //封装用户信息
                BeanUtils.copyProperties(userInfo, todayBestVo);
                //设置tags
                if (userInfo.getTags() != null) {
                    todayBestVo.setTags(userInfo.getTags().split(","));
                }
                //设置缘分值（调用Api查询的结果）
                todayBestVo.setFateValue(recommendUser.getScore().longValue());

                //把封装好的vo对象，添加到集合
                result.add(todayBestVo);
            }
        }

        //4.3 把封装好的vo设置到pageResult中
        pageResult.setItems(result);

        //4. 把封装好的TodayBestVo设置到响应体中，返回给前端
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 接口名称：佳人信息
     */
    public ResponseEntity<Object> queryPersonalInfo(Long recommendUserId) {
        //1. 根据推荐用户id查询
        UserInfo userInfo = userInfoApi.findById(recommendUserId);
        //2. 创建vo，封装返回结果
        TodayBestVo todayBestVo = new TodayBestVo();
        //2.1 封装用户信息
        BeanUtils.copyProperties(userInfo,todayBestVo);
        todayBestVo.setTags(userInfo.getTags().split(","));
        //2.2 查询缘分值并封装
        long score = recommendUserApi.queryScore(UserHolder.getUserId(),recommendUserId);
        todayBestVo.setFateValue(score);
        return ResponseEntity.ok(todayBestVo);
    }

    /**
     * 接口名称：查询陌生人问题
     */
    public ResponseEntity<Object> strangerQuestions(Long userId) {
        Question question = questionApi.findByUserId(userId);
        String text = question != null ? question.getTxt() : "你喜欢什么？..";
        return ResponseEntity.ok(text);
    }

    /**
     * 接口名称：搜附近
     */
    public ResponseEntity<Object> searchNear(String gender, Long distance) {
        //1. 获取用户id
        Long userId = UserHolder.getUserId();

        //2. 调用api查询附近人
        // 为什么不能返回UserLocation：因为其中的GeoJsonPoint对象没有实现序列化接口
        // 所以，需要定义UserLocationVo并返回此对象
        List<UserLocationVo> locationList = userLocationApi.searchNear(userId,distance);
        //3. 返回结果
        List<NearUserVo> voList = new ArrayList<>();
        //4. 封装voList返回结果
        if (locationList != null && locationList.size()>0) {
            for (UserLocationVo userLocationVo : locationList) {
                // 搜附近：不能包含自己
                if (userLocationVo.getUserId() == userId) {
                    continue;
                }
                // 搜附件： 根据gender筛选
                UserInfo userInfo = userInfoApi.findById(userLocationVo.getUserId());
                if (userInfo != null) {
                    if (!gender.equals(userInfo.getGender())) {
                        continue;
                    }
                    // 满足条件，返回
                    NearUserVo nearUserVo = new NearUserVo();
                    nearUserVo.setUserId(userInfo.getId());
                    nearUserVo.setAvatar(userInfo.getAvatar());
                    nearUserVo.setNickname(userInfo.getNickname());
                    voList.add(nearUserVo);
                }
            }
        }
        System.out.println("voList = " + voList);
        return ResponseEntity.ok(voList);
    }

    /**
     * 接口名称：探花-左滑右滑
     * @return
     */
    public ResponseEntity<Object> cards() {
        Long userId = UserHolder.getUserId();
        List<UserInfo> userInfoList = userInfoApi.findAll(userId);
        List<TanhuaVo> tanhuaVoList = new ArrayList<>();

        for (UserInfo userInfo : userInfoList) {
            TanhuaVo tanhuaVo = new TanhuaVo();
            tanhuaVo.setId(userInfo.getId().intValue());
            tanhuaVo.setAvatar(userInfo.getAvatar());
            tanhuaVo.setNickname(userInfo.getNickname());
            tanhuaVo.setGender(userInfo.getGender());
            tanhuaVo.setAge(userInfo.getAge());
            tanhuaVo.setTags(userInfo.getTags().split(","));
            tanhuaVoList.add(tanhuaVo);
        }
        return ResponseEntity.ok(tanhuaVoList);
    }

    /**
     * 接口名称：探花-不喜欢
     * @param likeUserId
     * @return
     */
    public ResponseEntity<Object> unlove(Long likeUserId) {
        //不喜欢别人，假如以前喜欢过就删除数据库数据，以前没喜欢过就忽略

        Long userId = UserHolder.getUserId();
        UserLike userLike = userLikeApi.findMyLike(userId, likeUserId);

        if(userLike != null) {
            userLikeApi.deleteLike(userId, likeUserId);
        }
        return ResponseEntity.ok(null);
    }

    public ResponseEntity<Object> love(Long likeUserId) {
        Long userId = UserHolder.getUserId();
        UserLike userLike = userLikeApi.findMyLike(userId, likeUserId);
        if(userLike == null) {
            userLikeApi.saveLike(userId, likeUserId);
        }

        return ResponseEntity.ok(null);
    }
}
