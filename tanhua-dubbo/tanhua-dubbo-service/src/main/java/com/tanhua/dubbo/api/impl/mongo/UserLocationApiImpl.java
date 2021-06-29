package com.tanhua.dubbo.api.impl.mongo;

import com.tanhua.domain.mongo.UserLocation;
import com.tanhua.domain.vo.UserLocationVo;
import com.tanhua.dubbo.api.mongo.UserLocationApi;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@Service(timeout = 300000)
public class UserLocationApiImpl implements UserLocationApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void saveLocation(Double latitude, Double longitude, String addrStr, Long userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        if (!mongoTemplate.exists(query, UserLocation.class)){
            UserLocation userLocation = new UserLocation();
            userLocation.setId(ObjectId.get());
            userLocation.setUserId(userId);
            userLocation.setLocation(new GeoJsonPoint(longitude,latitude));
            userLocation.setAddress(addrStr);
            userLocation.setCreated(System.currentTimeMillis());
            userLocation.setUpdated(System.currentTimeMillis());
            userLocation.setLastUpdated(System.currentTimeMillis());
            mongoTemplate.save(userLocation);
        }else{
            Update update = new Update();
            update.set("address",addrStr);
            update.set("updated",System.currentTimeMillis());
            update.set("lastUpdated",System.currentTimeMillis());
            update.set("location",new GeoJsonPoint(longitude,latitude));
            mongoTemplate.updateFirst(query,update,UserLocation.class);
        }
    }

    @Override
    public List<UserLocationVo> searchNear(Long userId, Long distance) {
        //1. 查询当前用户的地理位置
        Query query = new Query(Criteria.where("userId").is(userId));
        UserLocation userLocation = mongoTemplate.findOne(query, UserLocation.class);
        // 获取用户的坐标
        GeoJsonPoint location = userLocation.getLocation();

        //2. 准备参数
        //2.1 画圆半径的距离
        Distance distanceObj = new Distance(distance/1000, Metrics.KILOMETERS);
        //2.2 根据用户的坐标画圆
        Circle circle = new Circle(location,distanceObj);

        //3. 根据当前用户的坐标画圆，搜索附近的人
        Query locationQuery = new Query(
                Criteria.where("location").withinSphere(circle)
        );
        List<UserLocation> locationList = mongoTemplate.find(locationQuery, UserLocation.class);
        return UserLocationVo.formatToList(locationList);
    }
}
