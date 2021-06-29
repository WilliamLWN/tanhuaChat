package com.tanhua.domain.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 评论表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quanzi_comment")
public class Comment implements Serializable {

    private static final long serialVersionUID = -291788258125767614L;

    private ObjectId id;

    private ObjectId publishId;    //发布id
    private Integer commentType;   //评论类型，1-点赞，2-评论，3-喜欢
    private Integer pubType;       //评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
    private String content;        //评论内容
    private Long userId;           //评论人
    //private Integer likeCount = 0; //点赞数
    private Long created; //发表时间

    //动态选择更新的字段
    public String getCol() {
        return this.commentType == 1 ? "likeCount" : commentType==2 ? "commentCount" : "loveCount";
    }
}
