package com.tanhua.domain.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "recommend_user")
public class RecommendUser implements Serializable {

    private static final long serialVersionUID = 5874126532504390567L;
    @Id
    private ObjectId id; //主键id
    @Indexed
    private Long recommendUserId; //推荐的用户id
    private Long userId; //用户id
    @Indexed
    private Double score =0d; //推荐得分
    private String date; //日期
}
