package com.tanhua.domain.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 问题表
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "question")
public class Question implements Serializable {

    private static final long serialVersionUID = 4321830950342165617L;

    private ObjectId questionId;  //题目id
    private String question;  //题目内容
    private String level;  //题目等级

//    private QuestionAnwser questionAnwser;  //题目答案选项
}
