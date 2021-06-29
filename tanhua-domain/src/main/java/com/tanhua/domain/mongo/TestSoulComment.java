package com.tanhua.domain.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 评语表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "testSoul_comment")
public class TestSoulComment implements Serializable {

    private static final long serialVersionUID = 4321830950922165617L;

    private Integer id; //评语id
    private String comment; //评语
    private String cover; //鉴定图片
    private List<Map<String,String>> dimensions; //维度

}
