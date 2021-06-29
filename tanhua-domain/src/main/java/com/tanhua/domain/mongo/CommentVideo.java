package com.tanhua.domain.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 视频点赞、评论
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "video_comment")
public class CommentVideo implements Serializable {

    private static final long serialVersionUID = -451788258125767614L;

    private ObjectId id;

    private ObjectId videoId;    //视频id
    private Integer commentType;   //评论类型，1-点赞，2-评论
    private Integer pubType;       //评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
    private String content;        //评论内容
    private Long userId;           //评论人
    private Long created; //发表时间

}
