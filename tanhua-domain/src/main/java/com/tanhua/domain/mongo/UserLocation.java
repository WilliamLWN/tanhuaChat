package com.tanhua.domain.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_location")
//为了提高查询效率，设置复合索引，2dsphere = 2D球面
@CompoundIndex(name = "location_index", def = "{'location': '2dsphere'}")
public class UserLocation implements Serializable {

    private static final long serialVersionUID = 4508868382007529970L;

    @Id
    private ObjectId id;
    @Indexed
    private Long userId; //用户id
    private Long created; //创建时间

    private GeoJsonPoint location; //x:经度 y:纬度  //此对象没有实现实现序列化接口
    private String address; //位置描述

    private Long updated; //更新时间
    private Long lastUpdated; //上次更新时间
}
