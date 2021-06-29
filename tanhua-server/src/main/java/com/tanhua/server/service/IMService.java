package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.*;
import com.tanhua.domain.vo.ContractVo;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.domain.vo.LikesVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.dubbo.api.mongo.FreezeApi;
import com.tanhua.dubbo.api.mongo.FriendApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.RelativeDateFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IMService {

    @Autowired
    private HuanXinTemplate huanXinTemplate;
    @Reference
    private UserInfoApi userInfoApi;
    @Reference
    private QuestionApi questionApi;
    @Reference
    private FriendApi friendApi;
    @Reference
    private CommentApi commentApi;
    @Reference
    private FreezeApi freezeApi;

    /**
     * 接口名称：回复陌生人问题
     * @param strangerId 陌生人用户id
     * @param reply 回复内容
     * @return
     */
    public ResponseEntity<Object> replyQuestions(Integer strangerId, String reply) {
        //1. 查询当前登陆用户信息
        UserInfo userInfo = userInfoApi.findById(UserHolder.getUserId());
        //2. 根据陌生人用户id查询问题
        Question question = questionApi.findByUserId(strangerId.longValue());

        //判断是否被冻结发言
        FreezeUser freezeUser = freezeApi.findByUserId(userInfo.getId().intValue());
        if(userInfo.getUserStatus() == 2 && freezeUser.getFreezingRange() == 2) {
            return ResponseEntity.status(500).body(ErrorResult.freezeChatError());
        }

        //3. 构建消息内容 (格式固定)
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userInfo.getId().toString());
        map.put("nickname", userInfo.getNickname());
        map.put("strangerQuestion", question==null?"你喜欢我吗.？":question.getTxt());
        map.put("reply", reply);
        //4. map转换为json字符串，并实现发消息
        String message = JSON.toJSONString(map);
        //5. 发消息
        huanXinTemplate.sendMsg(strangerId.toString(), message);
        return ResponseEntity.ok(null);
    }

    /**
     * 接口名称：联系人添加
     * @param friendId 好友ID
     * @return
     */
    public ResponseEntity<Object> addContract(Long friendId) {
        //1. 保存好友关系到mongodb中
        friendApi.save(UserHolder.getUserId(),friendId);
        //2. 好友关系注册到环信
        huanXinTemplate.contactUsers(UserHolder.getUserId(),friendId);
        return ResponseEntity.ok(null);
    }

    /**
     * 接口名称：联系人列表
     */
    public ResponseEntity<Object> queryContractList(
            Integer page, Integer pagesize, String keyword) {
        //1. 分页查询当前用户的好友列表
        PageResult pageResult = friendApi.findFriendByUserId(page,pagesize,keyword,UserHolder.getUserId());
        //2. 获取好友数据
        List<Friend> friendList = (List<Friend>) pageResult.getItems();
        //3. 构造返回的结果数据
        List<ContractVo> voList = new ArrayList<>();
        //4. 遍历查询结果，封装返回数据
        if (friendList != null && friendList.size() > 0) {
            friendList.forEach((friend)->{
                ContractVo contractVo = new ContractVo();
                UserInfo userInfo = userInfoApi.findById(friend.getFriendId());
                BeanUtils.copyProperties(userInfo,contractVo);
                // 获取userInfo中的如“北京市”。举例：北京市-北京城区-东城区只取北京市
                contractVo.setCity(StringUtils.substringBefore(userInfo.getCity(),"-"));
                // 注意：两个对象中的userId类型不一样，需要手动去设置，这里的userid指的是好友ID
                contractVo.setUserId(friend.getFriendId().toString());
                voList.add(contractVo);
            });
        }
        //5. 设置集合到分页对象中
        pageResult.setItems(voList);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 接口名称：点赞列表
     */
    public ResponseEntity<Object> queryLikesList(
            Integer page, Integer pagesize) {

        //1. 分页查询当前用户的好友列表
        PageResult pageResult = commentApi.findLikesByUserId(page,pagesize, UserHolder.getUserId());
        //2. 获取好友数据
        List<LikesPublish> likesPublishList = (List<LikesPublish>) pageResult.getItems();
        //3. 构造返回的结果数据
        List<LikesVo> voList = new ArrayList<>();
        //4. 遍历查询结果，封装返回数据
        if (likesPublishList != null && likesPublishList.size() > 0) {
            for (LikesPublish likesPublish : likesPublishList) {
                LikesVo likesVo = new LikesVo();
                UserInfo userInfo = userInfoApi.findById(likesPublish.getUserId());
                BeanUtils.copyProperties(userInfo,likesVo);
                likesVo.setId(userInfo.getId().toString());
                likesVo.setCreateDate(RelativeDateFormat.format(new Date(likesPublish.getCreated())));
                voList.add(likesVo);
            }
        }
        //5. 设置集合到分页对象中
        pageResult.setItems(voList);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 接口名称：评论列表
     */
    public ResponseEntity<Object> queryCommentsList(
            Integer page, Integer pagesize) {

        //1. 分页查询当前用户的好友列表
        PageResult pageResult = commentApi.findCommentsByUserId(page,pagesize, UserHolder.getUserId());
        //2. 获取好友数据
        List<CommentPublish> commentPublishList = (List<CommentPublish>) pageResult.getItems();
        //3. 构造返回的结果数据
        List<LikesVo> voList = new ArrayList<>();
        //4. 遍历查询结果，封装返回数据
        if (commentPublishList != null && commentPublishList.size() > 0) {
            for (CommentPublish commentPublish : commentPublishList) {
                LikesVo likesVo = new LikesVo();
                UserInfo userInfo = userInfoApi.findById(commentPublish.getUserId());
                BeanUtils.copyProperties(userInfo,likesVo);
                likesVo.setId(userInfo.getId().toString());
                likesVo.setCreateDate(RelativeDateFormat.format(new Date(commentPublish.getCreated())));
                voList.add(likesVo);
            }
        }
        //5. 设置集合到分页对象中
        pageResult.setItems(voList);
        return ResponseEntity.ok(pageResult);
    }

    public ResponseEntity<Object> queryLovesList(Integer page, Integer pagesize) {

        //1. 分页查询当前用户的好友列表
        PageResult pageResult = commentApi.findLovesByUserId(page,pagesize, UserHolder.getUserId());
        //2. 获取好友数据
        List<LovesPublish> lovesPublishList = (List<LovesPublish>) pageResult.getItems();
        //3. 构造返回的结果数据
        List<LikesVo> voList = new ArrayList<>();
        //4. 遍历查询结果，封装返回数据
        if (lovesPublishList != null && lovesPublishList.size() > 0) {
            for (LovesPublish lovesPublish : lovesPublishList) {
                LikesVo likesVo = new LikesVo();
                UserInfo userInfo = userInfoApi.findById(lovesPublish.getUserId());
                BeanUtils.copyProperties(userInfo,likesVo);
                likesVo.setId(userInfo.getId().toString());
                likesVo.setCreateDate(RelativeDateFormat.format(new Date(lovesPublish.getCreated())));
                voList.add(likesVo);
            }
        }
        //5. 设置集合到分页对象中
        pageResult.setItems(voList);
        return ResponseEntity.ok(pageResult);
    }
}
