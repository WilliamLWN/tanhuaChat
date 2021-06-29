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
 * 测灵魂结果表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "testSoul_conclusion")
public class TestSoulConclusion implements Serializable {

    private static final long serialVersionUID = 4321870950922165617L;

    private ObjectId id; //结果id
    private Long userId; //用户id
    private String gender; //性别、
    private String level; //试题级别
    private Integer type;  //类型
    private String conclusion; //结论
    private String cover; //对应图片
    private List<Map<String,String>> dimensions; //维度
    private List<Map<String,Object>> similarYou; //与你相似
    private Long created;
}
