package com.tanhua.domain.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * 冻结用户表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "freeze_users")
public class FreezeUser implements Serializable {

    private static final long serialVersionUID = 6003135946820874860L;
    private ObjectId id;
    private Integer userId; //用户id
    private Integer freezingTime; //冻结时间，1为冻结3天，2为冻结7天，3为永久冻结
    private Integer freezingRange; //冻结范围，1为冻结登录，2为冻结发言，3为冻结发布动态
    private String reasonsForFreezing; ////冻结原因
    private String frozenRemarks; //冻结备注
    private Integer state; //1为冻结中，2为已解封
    private Long created; //创建时间
    private String reasonsForThawing; //解封理由
}
