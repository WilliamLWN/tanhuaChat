package com.tanhua.domain.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "likes_publish")
public class LikesPublish implements Serializable {

    private static final long serialVersionUID = 8732308321082804772L;

    private ObjectId publishId;    //发布id
    private Integer commentType;   //评论类型，1-点赞，2-评论，3-喜欢
    private Integer pubType;       //评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
    private String content;        //评论内容
    private Long userId;           //评论人
    //private Integer likeCount = 0; //点赞数
    private Long created; //发表时间
}
