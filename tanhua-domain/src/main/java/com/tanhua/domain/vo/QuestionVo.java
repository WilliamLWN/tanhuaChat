package com.tanhua.domain.vo;

import com.tanhua.domain.mongo.QuestionAnwser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionVo {

    private String id;  //题目id
    private String question;  //题目内容
    private List<Map<String, String>> options;  //题目答案选项
}
