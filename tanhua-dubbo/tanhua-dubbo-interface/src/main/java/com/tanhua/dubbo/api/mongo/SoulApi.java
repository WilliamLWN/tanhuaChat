package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Question;
import com.tanhua.domain.mongo.QuestionAnwser;
import com.tanhua.domain.mongo.TestSoulComment;
import com.tanhua.domain.mongo.TestSoulConclusion;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

public interface SoulApi {
    void saveQuestionAnwser(QuestionAnwser questionAnwser);

    void saveQuestion(Question question);

    void saveComment(TestSoulComment testSoulComment);

    List<Question> findQuestionLow();

    List<QuestionAnwser> findQuestionAnwser(ObjectId questionId);

    Integer findQuestionAnwserScore(String questionId, String optionId);

    TestSoulComment findSoulComment(Integer score);

    String findQuestionLevel(String questionId);

    List<TestSoulConclusion> findConclusionSimilar(Integer id, Long userId, String level);

    void saveTestSoulConclusion(TestSoulConclusion testSoulConclusion);

    TestSoulConclusion findConclusionById(String reportId);

    boolean findConclusion(Long userId, String level);

    List<Question> findQuestionMiddle();

    List<Question> findQuestionHigh();
}
