package com.tanhua.manage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.FreezeUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.vo.*;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.dubbo.api.mongo.FreezeApi;
import com.tanhua.dubbo.api.mongo.PublishApi;
import com.tanhua.dubbo.api.mongo.VideoApi;
import com.tanhua.manage.domain.Admin;
import com.tanhua.manage.mapper.AdminMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

@Service
public class UserService extends ServiceImpl<AdminMapper, Admin> {

    @Reference
    private UserInfoApi userInfoApi;
    @Reference
    private VideoApi videoApi;
    @Reference
    private PublishApi publishApi;
    @Reference
    private CommentApi commentApi;
    @Reference
    private FreezeApi freezeApi;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 接口名称：用户数据翻页
     */
    public ResponseEntity<Object> findByPage(Integer page, Integer pagesize) {
        IPage<UserInfo> iPage = userInfoApi.findByPage(page,pagesize);
        List<UserInfo> userInfoList = iPage.getRecords();
        List<UserInfoForManageVo> userInfoForManageVoList = new ArrayList<>();
        for (UserInfo userInfo : userInfoList) {
            UserInfoForManageVo userInfoForManageVo = new UserInfoForManageVo();
            BeanUtils.copyProperties(userInfo, userInfoForManageVo);
            userInfoForManageVo.setUserStatus(String.valueOf(userInfo.getUserStatus()));
            userInfoForManageVoList.add(userInfoForManageVo);
        }
        PageResult pageResult = new PageResult(page,pagesize, (int) iPage.getTotal(),userInfoForManageVoList);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 接口名称：用户基本资料
     */
    public ResponseEntity<Object> findById(Long userId) {
        UserInfo userInfo = userInfoApi.findById(userId);
        UserInfoForManageVo userInfoForManageVo = new UserInfoForManageVo();
        BeanUtils.copyProperties(userInfo, userInfoForManageVo);
        userInfoForManageVo.setUserStatus(String.valueOf(userInfo.getUserStatus()));
        return ResponseEntity.ok(userInfoForManageVo);
    }

    /**
     * 接口名称：视频记录翻页
     * @param page
     * @param pagesize
     * @param uid
     * @return
     */
    public ResponseEntity<Object> findVideosList(Integer page, Integer pagesize, Long uid) {
        //1、分页查询
        PageResult pageResult = videoApi.findByPage(page, pagesize, uid);
        //2、获取分页数据中数据列表   List<Video>
        List<Video> items = (List<Video>) pageResult.getItems();
        //3、循环遍历数据集合，一个video构造一个vo
        List<VideoVo> list = new ArrayList<>();
        if(items != null) {
            for (Video item : items) {
                UserInfo info = userInfoApi.findById(item.getUserId());
                VideoVo vo = new VideoVo();
                BeanUtils.copyProperties(info,vo);
                BeanUtils.copyProperties(item,vo);
                vo.setCover(item.getPicUrl());
                vo.setId(item.getId().toHexString());
                vo.setSignature(item.getText());//签名
                list.add(vo);
            }
        }
        //4、存入pageResult中
        pageResult.setItems(list);
        //5、构造返回值
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 接口名称：动态分页
     * @param page
     * @param pagesize
     * @param uid
     * @param state
     * @return
     */
    public ResponseEntity<Object> findMovementsList(Integer page, Integer pagesize, Long uid, Long state) {
        //调用API查询publish
        PageResult pageResult = publishApi.findPublishList(page,pagesize,uid,state);
        //3. 获取用户好友动态数据
        List<Publish> items = (List<Publish>) pageResult.getItems();

        //4. 构造返回的集合数据
        List<MovementsVo> resultList = new ArrayList<>();
        if (items != null && items.size() > 0) {
            items.forEach(publish -> {
                //4.1 创建要封装的vo对象
                MovementsVo vo = new MovementsVo();
                //4.2 封装发布动态信息
                BeanUtils.copyProperties(publish, vo);
                //4.3 封装用户信息
                Long userId = publish.getUserId();
                UserInfo userInfo = userInfoApi.findById(userId);
                if (userInfo != null) {
                    BeanUtils.copyProperties(userInfo, vo);
                    if (!StringUtils.isEmpty(userInfo.getTags())) {
                        vo.setTags(userInfo.getTags().split(","));
                    }
                }
                //4.4 其他参数
                vo.setId(publish.getId().toHexString());
                vo.setCreateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(publish.getCreated())));
                vo.setImageContent(publish.getMedias().toArray(new String[]{}));
                vo.setDistance("50米");
                resultList.add(vo);
            });
        }
        //5. 设置集合数据到分页对象中
        pageResult.setItems(resultList);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 接口名称：动态详情
     * @param publishId
     * @return
     */
    public ResponseEntity<Object> findMovementsById(String publishId) {
        Publish publish = publishApi.findById(publishId);
        //4.1 创建要封装的vo对象
        MovementsVo vo = new MovementsVo();
        //4.2 封装发布动态信息
        BeanUtils.copyProperties(publish, vo);
        //4.3 封装用户信息
        Long userId = publish.getUserId();
        UserInfo userInfo = userInfoApi.findById(userId);
        if (userInfo != null) {
            BeanUtils.copyProperties(userInfo, vo);
            if (!StringUtils.isEmpty(userInfo.getTags())) {
                vo.setTags(userInfo.getTags().split(","));
            }
        }
        //4.4 其他参数
        vo.setId(publish.getId().toHexString());
        vo.setCreateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(publish.getCreated())));
        vo.setImageContent(publish.getMedias().toArray(new String[]{}));
        vo.setDistance("50米");
        return ResponseEntity.ok(vo);
    }

    /**
     * 接口名称：评论列表翻页
     * @param publishId
     * @param page
     * @param pagesize
     * @return
     */
    public ResponseEntity<Object> findCommentsById(String publishId, Integer page, Integer pagesize) {
        //1. 根据动态id分页查询评论
        PageResult pageResult = commentApi.queryCommentsList(publishId,page,pagesize);
        //2. 获取评论数据
        List<Comment> commentList = (List<Comment>) pageResult.getItems();
        //3. 构造封装返回的vo集合。把查询的集合转换为vo集合
        List<CommentVo> commentVoList = new ArrayList<>();
        if (commentList != null) {
            commentList.forEach((comment -> {
                CommentVo commentVo = new CommentVo();
                BeanUtils.copyProperties(comment,commentVo);
                commentVo.setId(comment.getId().toString());
                commentVo.setCreateDate(new DateTime(comment.getCreated()).toString("yyyy年MM月dd日 HH:mm"));
                commentVo.setHasLiked(0);

                UserInfo userInfo = userInfoApi.findById(comment.getUserId());
                BeanUtils.copyProperties(userInfo,commentVo);

                commentVoList.add(commentVo);
            }));
        }
        //4. 设置封装的集合
        pageResult.setItems(commentVoList);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 接口名称：动态通过
     * @param items 动态id列表
     * @return
     */
    public ResponseEntity<Object> passMovements(String[] items) {
        for (String publishId : items) {
            Integer state = 1;
            publishApi.updateState(publishId, state);
        }
        Map<String, String> message = new HashMap<>();
        message.put("message", "动态通过成功!");
        return ResponseEntity.ok(message);
    }

    /**
     * 接口名称：动态拒绝
     * @param items
     * @return
     */
    public ResponseEntity<Object> rejectMovements(String[] items) {
        for (String publishId : items) {
            Integer state = 2;
            publishApi.updateState(publishId, state);
        }
        Map<String, String> message = new HashMap<>();
        message.put("message", "动态拒绝成功!");
        return ResponseEntity.ok(message);
    }

    /**
     * 接口名称：用户冻结操作
     * 接口路径：POST/manage/users/freeze
     */
    public ResponseEntity<Object> freeze(Map<String, String> param) {
        Integer userId = Integer.valueOf(param.get("userId"));  //用户id
        Integer freezingTime = Integer.valueOf(param.get("freezingTime"));  //冻结时间，1为冻结3天，2为冻结7天，3为永久冻结
        Integer freezingRange = Integer.valueOf(param.get("freezingRange"));  //冻结范围，1为冻结登录，2为冻结发言，3为冻结发布动态
        String reasonsForFreezing = param.get("reasonsForFreezing");  //冻结原因
        String frozenRemarks = param.get("frozenRemarks");  //冻结备注

        FreezeUser freezeUser = new FreezeUser();
        freezeUser.setId(ObjectId.get());
        freezeUser.setUserId(userId);
        freezeUser.setFreezingTime(freezingTime);
        freezeUser.setFreezingRange(freezingRange);
        freezeUser.setReasonsForFreezing(reasonsForFreezing);
        freezeUser.setFrozenRemarks(frozenRemarks);
        freezeUser.setState(1);
        freezeUser.setCreated(new Date().getTime());

        //冻结用户 UserStatus=2
        boolean result = userInfoApi.freezeUserStatus(userId);
        if(result) {
            String FREEZE_KEY = "FREEZE_KEY_";
            switch (freezingTime) {
                case 1:
                    redisTemplate.opsForValue().set(FREEZE_KEY + userId, reasonsForFreezing, Duration.ofDays(3));
                    freezeApi.save(freezeUser);
                    break;
                case 2:
                    redisTemplate.opsForValue().set(FREEZE_KEY + userId, reasonsForFreezing, Duration.ofDays(7));
                    freezeApi.save(freezeUser);
                    break;
                case 3:
                    redisTemplate.opsForValue().set(FREEZE_KEY + userId, reasonsForFreezing);
                    freezeApi.save(freezeUser);
                    break;
                default:
                    break;
            }
        }
        Map<String, String> message = new HashMap<>();
        message.put("message", "冻结操作成功！");
        return ResponseEntity.ok(message);
    }

    /**
     * 接口名称：用户解冻操作
     * @param param
     * @return
     */
    public ResponseEntity<Object> unfreeze(Map<String, String> param) {
        Integer userId = Integer.valueOf(param.get("userId"));
        String reasonsForThawing = param.get("reasonsForThawing");

        FreezeUser freezeUser = freezeApi.findByUserId(userId);
        freezeUser.setState(2); //解封
        freezeUser.setReasonsForThawing(reasonsForThawing);
        freezeApi.unfreeze(freezeUser);

        String FREEZE_KEY = "FREEZE_KEY_";
        if (redisTemplate.hasKey(FREEZE_KEY + userId)) {
            redisTemplate.delete(FREEZE_KEY + userId);
        }
        UserInfo userInfo = userInfoApi.findById(freezeUser.getUserId().longValue());
        Integer state = 1;
        userInfo.setUserStatus(state);
        userInfoApi.update(userInfo);
        return ResponseEntity.ok(new HashMap<String,String>().put("message", "解封操作成功！"));
    }
}
