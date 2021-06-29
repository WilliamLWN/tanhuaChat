package com.tanhua.domain.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("video_score")
public class VideoScore {

    private ObjectId id;
    private Long userId;// 用户id
    private Long videoId; //视频id （vid）
    private Double score; //得分
    private Long date; //时间戳
}
