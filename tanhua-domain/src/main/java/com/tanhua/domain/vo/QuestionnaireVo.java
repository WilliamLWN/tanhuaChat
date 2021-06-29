package com.tanhua.domain.vo;

import com.tanhua.domain.mongo.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireVo {

    private String id; //问卷id
    private String name; //问卷名称
    private String cover; //封面
    private String level; //级别
    private Integer star; //星别（例如：2颗星，3颗星，5颗星）
    private List<QuestionVo> questions; //试题
    private Integer isLock; //是否锁住（0解锁，1锁住）
    private String reportId; //最新报告id
}
