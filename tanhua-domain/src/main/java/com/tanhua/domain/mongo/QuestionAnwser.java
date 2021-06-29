package com.tanhua.domain.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 答案表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "question_answer")
public class QuestionAnwser implements Serializable {

    private static final long serialVersionUID = 4312830950922165617L;

    private ObjectId questionId; //问题id
    private String optionId; //答案选项  A B C D
    private String option; //选项内容
    private Integer score; //选项分数

}
