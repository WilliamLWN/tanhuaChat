package com.tanhua.domain.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sequence")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sequence {  //序列对象，针对不同的表可以实现自增长

    private ObjectId id;
    // 自增长值
    private long seqId;
    // 区分mongo中的不同集合名称
    private String collName;
}
