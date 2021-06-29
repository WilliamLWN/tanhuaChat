package com.tanhua.server.service;

import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.mongo.FreezeUser;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Visitors;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.domain.vo.MovementsVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.VisitorsVo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.FreezeApi;
import com.tanhua.dubbo.api.mongo.PublishApi;
import com.tanhua.dubbo.api.mongo.VisitorsApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.RelativeDateFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MovementsService {
    @Autowired
    private OssTemplate ossTemplate;
    @Reference
    private PublishApi publishApi;
    @Reference
    private UserInfoApi userInfoApi;
    @Reference
    private VisitorsApi visitorsApi;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Reference
    private FreezeApi freezeApi;
    /**
     * 接口名称：动态-发布
     */
    public ResponseEntity<Object> saveMovements(Publish publish, MultipartFile[] imageContent) throws IOException {
        //1. 获取登陆用户id
        Long userId = UserHolder.getUserId();

        //判断是否被冻结
        UserInfo userInfo = userInfoApi.findById(userId);
        FreezeUser freezeUser = freezeApi.findByUserId(userId.intValue());
        if(userInfo.getUserStatus() == 2 && freezeUser.getFreezingRange() == 3) {
            return null;
        }

        //2. 处理文件上传，并封装上传的图片数组对象
        List<String> medias = new ArrayList<>();
        if (imageContent != null) {
            for (MultipartFile multipartFile : imageContent) {
                String url = ossTemplate.upload(
                        multipartFile.getOriginalFilename(), multipartFile.getInputStream());
                medias.add(url);
            }
        }

        //3. 设置参数
        publish.setMedias(medias);
        publish.setUserId(userId);

        //4. （发布）保存动态
        //【发送消息到mq中：要获取到动态id。所以在这里设置动态id。注意：api中要取消设置】
        publish.setId(ObjectId.get());	//-------------------添加的代码
        //【发布动态，默认状态为未审核】
        publish.setState(0); 			//-------------------添加的代码
        publishApi.save(publish);

        //【发送消息到mq中，在tanhua-manage消息处理系统中，审核文本、图片】 //-------添加的代码
        try {
            rocketMQTemplate.convertAndSend("tanhua-publish",publish.getId().toString());
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(null);
    }

    /**
     * 接口名称：好友动态
     */
    public ResponseEntity<Object> queryPublishList(Integer page, Integer pagesize) {
        //1. 获取登陆用户id
        Long userId = UserHolder.getUserId();

        //2. 分页查询好友动态（自己的时间线表）
        PageResult pageResult = publishApi.findByTimeLine(page,pagesize,userId);
        List<MovementsVo> voList = setMovementsVo(pageResult);
        //4. 在把封装好的vo集合设置到分页对象中
        pageResult.setItems(voList);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 接口名称：推荐动态
     */
    public ResponseEntity<Object> queryRecommendPublishList(Integer page, Integer pagesize) {
//        //1. 获取登陆用户id
//        Long userId = UserHolder.getUserId();
//
//        //2. 分页查询推荐动态集合
//        PageResult pageResult = publishApi.queryRecommendPublishList(page,pagesize,userId);
//
//        System.out.println("pageResult = " + pageResult);
//
//        //3. 调用抽取的方法，把List<Publish>封装为List<MovementsVo>并设置到PageResult中
//        List<MovementsVo> voList = setMovementsVo(pageResult);
//        pageResult.setItems(voList);
//        return ResponseEntity.ok(pageResult);

        //之前是通过MongoDB直接查询，而现在需要先从Redis进行命中推荐，如果未命中则需要进行MongoDB查询(mac不支持推荐系统的docker容器)
        //1、获取登录用户的ID
        Long userId = UserHolder.getUserId();
        //2、调用api完成分页查询   publish对象
        //a.查询redis中的数据（真正的推荐数据）
//        PageResult result = findByRecommend(page,pagesize);
//        if(result == null) {
            //b.查询默认的数据
          PageResult  result = publishApi.queryRecommendPublishList(page, pagesize, userId); //默认数据
//        }
        //3、获取publish列表
        List<Publish> items = (List<Publish>) result.getItems();
        //4、一个publish构造成一个Movements
        List<MovementsVo> list = new ArrayList<>();
        if(items != null) {
            for (Publish item : items) {
                MovementsVo vo = new MovementsVo();
                UserInfo userInfo = userInfoApi.findById(item.getUserId());
                if(userInfo != null) {
                    BeanUtils.copyProperties(userInfo,vo);
                    if(userInfo.getTags() != null) {
                        vo.setTags(userInfo.getTags().split(","));
                    }
                }
                BeanUtils.copyProperties(item, vo);
                vo.setId(item.getId().toHexString());
                vo.setCreateDate(RelativeDateFormat.format(new Date(item.getCreated())));
                if(redisTemplate.hasKey("publish_like_comment_"+userId+"_"+item.getId().toHexString())) {
                    vo.setHasLiked(1);  //是否点赞  0：未点 1:点赞
                }else{
                    vo.setHasLiked(0);  //是否点赞  0：未点 1:点赞
                }
                if(redisTemplate.hasKey("publish_love_comment_"+userId+"_"+item.getId().toHexString())) {
                    vo.setHasLoved(1);  //是否喜欢  0：未点 1:点赞
                }else {
                    vo.setHasLoved(0);  //是否喜欢  0：未点 1:点赞
                }
                vo.setImageContent(item.getMedias().toArray(new String[]{}));
                vo.setDistance("50米");
                list.add(vo);
            }
        }
        //5、构造返回值
        result.setItems(list);
        return ResponseEntity.ok(result);
    }

    public ResponseEntity<Object> queryMyAlbumList(Integer page, Integer pagesize, String id) {
        //1. 获取登陆用户id
        Long userId = UserHolder.getUserId();

//        if(userId != Long.parseLong(id)) {
//            return ResponseEntity.status(500).body(ErrorResult.error());
//        }
        //2. 分页查询推荐动态集合
        PageResult pageResult = publishApi.queryMyAlbumList(page,pagesize,userId);
        System.out.println("pageResult = " + pageResult);

        //3. 调用抽取的方法，把List<Publish>封装为List<MovementsVo>并设置到PageResult中
        List<MovementsVo> voList = setMovementsVo(pageResult);
        pageResult.setItems(voList);
        return ResponseEntity.ok(pageResult);
    }

    private List<MovementsVo> setMovementsVo(PageResult pageResult) {
        List<Publish> publishList = (List<Publish>) pageResult.getItems();

        //3. 封装返回的结果MovementsVo集合
        //3.1 创建返回结果
        List<MovementsVo> voList = new ArrayList<>();
        //3.2 遍历查询的发布动态集合
        if (publishList != null) {
            for (Publish publish : publishList) {
                //3.2.1 创建vo对象
                MovementsVo vo = new MovementsVo();
                //3.2.2 封装数据：发布动态
                BeanUtils.copyProperties(publish,vo);
                //3.2.3 封装数据：先查询用户详情，再封装
                UserInfo userInfo = userInfoApi.findById(publish.getUserId());
                if (userInfo != null) {
                    BeanUtils.copyProperties(userInfo,vo);
                    if (userInfo.getTags() != null) {
                        vo.setTags(userInfo.getTags().split(","));
                    }
                }
                //3.2.3 封装数据：其他参数
                vo.setId(publish.getId().toString());
                vo.setUserId(publish.getUserId());
                vo.setImageContent(publish.getMedias().toArray(new String[]{}));
                vo.setDistance("50米");
                vo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));

                // 获取redis中存储的当前用户的点赞标记
                String likeKey = "public_like_comment_" + UserHolder.getUserId() + "_" + vo.getId(); //vo.getId()实质是publish.getId().toString()
                String loveKey = "public_love_comment_" + UserHolder.getUserId() + "_" + vo.getId(); //vo.getId()实质是publish.getId().toString()
                if (redisTemplate.hasKey(likeKey)) {
                    vo.setHasLiked(1);  // 点赞过
                } else {
                    vo.setHasLiked(0);  // 未赞过
                }

                if (redisTemplate.hasKey(loveKey)) {
                    vo.setHasLoved(1);  // 点赞过
                } else {
                    vo.setHasLoved(0);  // 未赞过
                }

                //3.2.4 对象添加到集合
                voList.add(vo);
            }
        }
        return voList;
    }

    /**
     * 接口名称：谁看过我
     */
    public ResponseEntity<Object> queryVisitors() {
        //1. 获取当前登陆用户
        Long userId = UserHolder.getUserId();

        //2. 查询谁看过我, 返回List<Visistors>
        List<Visitors> visitorsList = null;

        //2.1 先从redis中获取访问时间 100
        String time = redisTemplate.opsForValue().get("visitor_time_" + userId);

        //2.2 第一次访问查看谁看过我，默认显示最近5个访客
        if (StringUtils.isEmpty(time)) {
            visitorsList = visitorsApi.queryVisitors(userId,5);
        } else {
            //2.3 查看最近的访客  date > 100
            visitorsList = visitorsApi.queryVisitors(userId,Long.parseLong(time));
        }

        //2.4 记录当前访问的时间  100
        redisTemplate.opsForValue().set("visitor_time_"+userId,System.currentTimeMillis()+"");

        //3. 封装前端返回的结果：List<VisitorsVo>
        List<VisitorsVo> voList = new ArrayList<>();
        if (visitorsList != null && visitorsList.size() > 0) {
            for (Visitors visitors : visitorsList) {
                //3.1 创建vo对象
                VisitorsVo vo = new VisitorsVo();
                //3.2 根据用户id查询
                UserInfo userInfo = userInfoApi.findById(visitors.getVisitorUserId());
                if (userInfo != null) {
                    BeanUtils.copyProperties(userInfo,vo);
                    if (userInfo.getTags() != null) {
                        vo.setTags(userInfo.getTags().split(","));
                    }
                }
                //3.3 设置缘分值
                vo.setFateValue(visitors.getScore().intValue());

                voList.add(vo);
            }
        }
        return ResponseEntity.ok(voList);
    }

    /**
     * 从redis查询推荐数据
     *  查询第一页，每页5条
     */
    public PageResult findByRecommend(Integer page, Integer pagesize) {
        //1、拼接redis中的key
        String key = "QUANZI_PUBLISH_RECOMMEND_"+UserHolder.getUserId();
        //2、从redis中获取数据
        String value = redisTemplate.opsForValue().get(key);
        //3、判断数据是否存在，如果不存在，返回null
        if(StringUtils.isEmpty(value)) {
            return null;
        }
        //4、如果数据存在，构造PageResult对象
        String pids[] = value.split(","); //1,2,3,4,5,6,7,8,9,10
        Integer counts = pids.length;
        int startIndex = (page - 1) * pagesize; //计算本页的起始条数

        if(startIndex < pids.length) { //起始条数小于数据总数
            int endIndex = startIndex + pagesize - 1;
            if (endIndex >= pids.length) {
                endIndex = pids.length - 1;
            }
            List<Long> pidList = new ArrayList<>();   //本页查询的所有动态的pid列表
            for (int i = startIndex; i <= endIndex; i++) {
                pidList.add(Long.valueOf(pids[i]));
            }
            //本次分页的数据列表
            List<Publish> list = publishApi.findByPids(pidList);
            return new PageResult(page, pagesize, counts, list);
        }
        return null;
    }
}
