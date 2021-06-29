package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.utils.StringUtils;
import com.tanhua.commons.templates.AipFaceTemplate;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.commons.templates.SmsTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.FreezeUser;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.domain.vo.UserLikeVo;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.domain.db.User;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.FreezeApi;
import com.tanhua.dubbo.api.mongo.FriendApi;
import com.tanhua.dubbo.api.mongo.UserLikeApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.JwtUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

/**
 * 注意：这里导入spring的包: org.springframework.stereotype.Service
 * 因为不是dubbo服务
 */
@Service
public class UserService {

    // 注入dubbo服务接口的代理对象
    @Reference
    private UserApi userApi;
    @Reference
    private UserInfoApi userInfoApi;
    @Reference
    private UserLikeApi userLikeApi;
    @Reference
    private FriendApi friendApi;
    @Reference
    private FreezeApi freezeApi;
    @Autowired
    private SmsTemplate smsTemplate;
    @Autowired
    private AipFaceTemplate aipFaceTemplate;
    @Autowired
    private OssTemplate ossTemplate;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private HuanXinTemplate huanXinTemplate;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    // 定义redis中存储的验证码的key的前缀
    private final String SMS_KEY = "SMS_KEY_";
    // 定义redis中存储的token的key的前缀
    private final String TOKEN_KEY = "TOKEN_KEY_";

    @Value("${tanhua.secret}")
    private String secret;

    /**
     * 发送短信验证码
     * @param phone 封装了手机号码,名称是phone
     * @return
     */
    public ResponseEntity<Object> sendSms(String phone) {
        //1. 生成6位随机数
        //String code = (int)((Math.random() * 9 + 1) * 100000) + "";
        String code = "123456";
        //2. 发送短信验证码
//        smsTemplate.sendSms(phone,code);
        //3. 验证码存储到redis中，key是: SMS_KEY_ + 手机号; 有效时间5分钟
        redisTemplate.opsForValue().set(SMS_KEY+phone,code, Duration.ofMinutes(5));
        return ResponseEntity.ok(null);
    }


    /**
     * 接口名称：登录第二步---验证码校验
     */
    public ResponseEntity<Object> loginVerification(String phone, String verificationCode) {
        //1. 从redis中获取验证码并校验验证码
        String redisCode = redisTemplate.opsForValue().get(SMS_KEY + phone);
        if (redisCode == null || !redisCode.equals(verificationCode)){
            return ResponseEntity.status(500).body(ErrorResult.loginError());
        }

        //2. 校验完毕，从redis中删除验证码
        redisTemplate.delete(SMS_KEY+phone);

        //3. 根据手机号码查询用户
        User user = userApi.findByMobile(phone);

        Long userId = 0L;
        boolean isNew = false;

        //【记录日志：操作类型默认为0101登陆】 --------------------------------------
        String type = "0101";
        //3.1 判断手机号码如果不存在，就自动注册
        if (user == null) {
            user = new User();
            user.setMobile(phone);
            user.setPassword(DigestUtils.md5Hex("123456"));
            // 保存用户
            userId = userApi.save(user);
            user.setId(userId);
            // 设置为新用户
            isNew = true;

            // 新用户同时注册到环信
            huanXinTemplate.register(user.getId());

            //【操作类型为注册】 --------------------------------------
            type = "0102";
        } else {
            //判断是否被冻结
            UserInfo userInfo = userInfoApi.findById(user.getId());
            FreezeUser freezeUser = freezeApi.findByUserId(user.getId().intValue());
            if(userInfo.getUserStatus() == 2 && freezeUser.getFreezingRange() == 1) {
                return ResponseEntity.status(500).body(ErrorResult.freezeLoginError());
            }
        }
        // 【准备日志数据】 --------------------------------------
        Map<String,String> map = new HashMap<>();
        map.put("userId",user.getId().toString());
        map.put("type",type);
        map.put("date",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        // 【发送mq消息】 ----------------最好添加try..catch异常处理------------
        rocketMQTemplate.convertAndSend("tanhua-log",JSON.toJSONString(map));

        // 【需求：生成token、存储token到redis中】
        String token = JwtUtils.createToken(user.getId(), phone, secret);
        // 【往redis中存储的应该是整个用户对象数据，所以这里需要把对象转换为json字符串存储】
        String tokenData = JSON.toJSONString(user);
        // 【生成的token作为key，用户数据作为value，存储到redis中，并设置过期时间】
        redisTemplate.opsForValue().set(TOKEN_KEY+token,tokenData,Duration.ofHours(4));

        //4. 构造返回结果
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("isNew",isNew);
        resultMap.put("token",token);
        return ResponseEntity.ok(resultMap);
    }

    /**
     * 接口名称：新用户---1填写资料
     * 需求描述：用户第一次登陆，需要完善用户信息
     */
    public ResponseEntity<Object> saveUserInfo(UserInfo userInfo) {
        //根据token，获取用户
        Long userId = UserHolder.getUserId();
        //设置用户详情id
        userInfo.setId(userId);
        //保存
        userInfoApi.save(userInfo);
        return ResponseEntity.ok(null);
    }

    /**
     * 完善用户信息 - 上传用户头像
     */
    public ResponseEntity<Object> setUserHead(MultipartFile headPhoto) throws IOException {
        //1. 根据token获取用户
        Long userId = UserHolder.getUserId();
        //2. 检测图片是否包含人脸
        boolean detect = aipFaceTemplate.detect(headPhoto.getBytes());
        if(!detect) {
            return ResponseEntity.status(500).body(ErrorResult.faceError());
        }
        //3. 图片上传到阿里云OSS
        String url = ossTemplate.upload(headPhoto.getOriginalFilename(), headPhoto.getInputStream());
        //4. 更新UserInfo，修改用户头像地址信息
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);
        userInfo.setAvatar(url);
        userInfoApi.update(userInfo); // dubbo服务接口需要添加update()方法并在服务工程中实现
        return ResponseEntity.ok(null);
    }

    /**
     * 接口名称：用户资料 - 读取
     * @return
     */
    public ResponseEntity<Object> findUserInfoById(Long userID,Long huanxinID) {

        // 从当前线程上直接获取用户id（拦截器已经处理）
        Long userId = UserHolder.getUserId();
        //2. 如果userID不为NULL，根据用户id查询
        if (userID != null) {
            userId = userID;
        }else if(huanxinID != null){
            userId = huanxinID;
        }
        //2. 根据用户id查询用户详情
        UserInfo userInfo = userInfoApi.findById(userId);
        //3. 创建封装返回结果的UserInfoVo对象用于返回前段
        UserInfoVo userInfoVo = new UserInfoVo();
        //BeanUtils只能把类型匹配的属性封装
        BeanUtils.copyProperties(userInfo,userInfoVo);
        //手动封装特殊类型的属性
        if (userInfo.getAge() != null) {
            userInfoVo.setAge(userInfo.getAge().toString());
        }
        return ResponseEntity.ok(userInfoVo);
    }

    /**
     * 接口名称：用户资料 - 保存
     */
    public ResponseEntity<Object> updateUserInfo(UserInfo userInfo) {

        Long userId = UserHolder.getUserId();
        //2. 设置用户明细中用户的id（请求中没有id参数）
        userInfo.setId(userId);
        //3. 修改
        userInfoApi.update(userInfo);
        return ResponseEntity.ok(null);
    }

    /**
     * 接口名称：用户头像 - 更改
     */
    public ResponseEntity<Object> updateUserHead(MultipartFile headPhoto) throws IOException {
        //1. 根据token获取用户
        Long userId = UserHolder.getUserId();
        //2. 检测图片是否包含人脸
        boolean detect = aipFaceTemplate.detect(headPhoto.getBytes());
        if(!detect) {
            return ResponseEntity.status(500).body(ErrorResult.faceError());
        }
        //3. 图片上传到阿里云OSS
        String url = ossTemplate.upload(headPhoto.getOriginalFilename(), headPhoto.getInputStream());
        //4. 更新UserInfo，修改用户头像地址信息
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);
        userInfo.setAvatar(url);
        userInfoApi.update(userInfo); // dubbo服务接口需要添加update()方法并在服务工程中实现
        return ResponseEntity.ok(null);
    }

//    /**
//     * 根据token查找用户
//     */
    public User findUserByToken(String token){
        //根据token，获取用户信息（json字符串）
        String userJsonString = redisTemplate.opsForValue().get(TOKEN_KEY + token);
        if(StringUtils.isEmpty(userJsonString)) {
            return null;
        }
        //把json字符串转换为对象
        User user = JSON.parseObject(userJsonString, User.class);
        //更新token有效时间
        redisTemplate.opsForValue().set(TOKEN_KEY+token,userJsonString,Duration.ofHours(4));
        return user;
    }

    public ResponseEntity<Object> findByMobile(String mobile){
        User user = userApi.findByMobile(mobile);
        // 通过ResponseEntity往响应体中写数据
        return ResponseEntity.ok(user);
    }

    public ResponseEntity<Object> save(User user){
        try {
            // 模拟异常
//            int i = 1/0;

            Long userId = userApi.save(user);
            // 正常返回
            return ResponseEntity.ok(userId);
        } catch (Exception e) {
            e.printStackTrace();
            // 异常返回：需要返回错误编码、错误信息
            Map<String, Object> resultMap = new HashMap<>();
            // 设置错误信息
            resultMap.put("errorCode",1000);
            resultMap.put("errMessage","对不起，我错了！");
            // 500 是http响应状态码，表示服务器异常
            return ResponseEntity.status(500).body(resultMap);
        }
    }

    public ResponseEntity<Object> queryCounts() {
        //1. 获取登陆用户id
        Long userId = UserHolder.getUserId();

        //2. 查询统计
        //2.1 统计互相喜欢
        Long eachLoveCount = userLikeApi.queryEachLoveCount(userId);
        //2.2 统计喜欢
        Long loveCount = userLikeApi.queryLoveCount(userId);
        //2.3 统计粉丝
        Long fanCount = userLikeApi.queryFanCount(userId);

        //3. 返回结果
        Map<String,Integer> resultMap = new HashMap<>();
        resultMap.put("eachLoveCount",eachLoveCount.intValue());
        resultMap.put("loveCount",loveCount.intValue());
        resultMap.put("fanCount",fanCount.intValue());
        return ResponseEntity.ok(resultMap);
    }

    /**
     * 接口名称：互相喜欢、喜欢、粉丝、谁看过我  (列表)
     * 互相喜欢:   db.tanhua_users.find({userId:20})
     * 喜欢:      db.user_like.find({userId:20})
     * 粉丝：     db.user_like.find({likeUserId:20})
     * 谁看过我 :  db.visitors.find({userId:20})
     */
    public ResponseEntity<Object> queryUserLikeList(Integer type, Integer page, Integer pagesize) {
        //1. 获取登陆用户id
        Long userId = UserHolder.getUserId();

        PageResult pageResult = null;
        //2. 根据类型判断
        switch (type) {
            case 1:
                pageResult = userLikeApi.queryEachLoveList(userId,page,pagesize);
                break;
            case 2:
                pageResult = userLikeApi.queryLoveList(userId,page,pagesize);
                break;
            case 3:
                pageResult = userLikeApi.queryFanList(userId,page,pagesize);
                break;
            case 4:
                pageResult = userLikeApi.queryVisitorList(userId,page,pagesize);
                break;
        }

        //3. 获取查询结果
        List<Map<String,Object>> list = (List<Map<String, Object>>) pageResult.getItems();

        //4. 封装返回结果
        List<UserLikeVo> voList = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (Map<String, Object> map : list) {
                // 获取用户id、缘分值
                Long uid = (Long) map.get("userId");
                Long score = (Long) map.get("score");

                // 创建vo
                UserLikeVo userLikeVo = new UserLikeVo();
                // 根据用户id查询
                UserInfo userInfo = userInfoApi.findById(uid);
                if (userInfo != null) {
                    BeanUtils.copyProperties(userInfo,userLikeVo);
                }
                // 设置缘分值
                userLikeVo.setMatchRate(score.intValue());
                // 添加到集合
                voList.add(userLikeVo);
            }
        }

        //5. 把封装的返回结果设置到pageRequest中
        pageResult.setItems(voList);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 接口名称：粉丝 - 喜欢
     * @param likeUserId 喜欢的用户id，如：登陆用户id=20，喜欢的用户likeUserId=3
     * @return
     */
    public ResponseEntity<Object> fansLike(Long likeUserId) {
        //1. 删除粉丝中的喜欢数据
        userLikeApi.delete(likeUserId,UserHolder.getUserId());
        //2. 记录双向的好友关系
        friendApi.save(UserHolder.getUserId(),likeUserId);
//        //3. 注册好友关系到环信
        huanXinTemplate.contactUsers(UserHolder.getUserId(),likeUserId);
        return ResponseEntity.ok(null);
    }

    /**
     * 接口名称：粉丝 - 取消喜欢
     * @param unlikeUserId 取消喜欢的用户id，如：登陆用户id=20，取消喜欢的用户likeUserId=3
     * @return
     */
    public ResponseEntity<Object> removeLike(Long unlikeUserId) {
        //1. 删除朋友中的数据
        friendApi.delete(unlikeUserId,UserHolder.getUserId());
        //2. 添加粉丝关系
        userLikeApi.save(UserHolder.getUserId(),unlikeUserId);
        return ResponseEntity.ok(null);
    }
}