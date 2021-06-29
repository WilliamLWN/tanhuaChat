package com.tanhua.domain.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "voice")
public class Voice implements Serializable {

    private static final long serialVersionUID = -3136732836884333873L;

    private ObjectId id; //主键id
    private Long vid; //自动增长
    private Long userId; //用户id
    private String gender; //性别
    private Integer age; //年龄
    private String soundUrl; //音频文件，URL
    private Long created; //创建时间
    private Integer state; //0:未被获取过  1:被获取过

}
