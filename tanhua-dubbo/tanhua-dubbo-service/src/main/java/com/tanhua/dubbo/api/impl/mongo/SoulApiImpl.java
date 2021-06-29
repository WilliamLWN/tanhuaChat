package com.tanhua.dubbo.api.impl.mongo;

import com.tanhua.domain.mongo.Question;
import com.tanhua.domain.mongo.QuestionAnwser;
import com.tanhua.domain.mongo.TestSoulComment;
import com.tanhua.domain.mongo.TestSoulConclusion;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.SoulApi;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SoulApiImpl implements SoulApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void saveQuestionAnwser(QuestionAnwser questionAnwser) {
        mongoTemplate.save(questionAnwser);
    }

    @Override
    public void saveQuestion(Question question) {
        mongoTemplate.save(question);
    }

    @Override
    public void saveComment(TestSoulComment testSoulComment) {
        mongoTemplate.save(testSoulComment);
    }

    @Override
    public List<Question> findQuestionLow() {
        Query query = new Query(new Criteria("level").is("初级"));
        return mongoTemplate.find(query, Question.class);
    }

    @Override
    public List<QuestionAnwser> findQuestionAnwser(ObjectId questionId) {
        Query query = new Query(new Criteria("questionId").is(questionId));
        return mongoTemplate.find(query, QuestionAnwser.class);
    }

    @Override
    public Integer findQuestionAnwserScore(String questionId, String optionId) {
        Integer score = 0;
        Query query = new Query(new Criteria("questionId").is(new ObjectId(questionId)).and("optionId").is(optionId));
        QuestionAnwser questionAnwser = mongoTemplate.findOne(query, QuestionAnwser.class);
        if(questionAnwser != null && questionAnwser.getScore()!= null) {
            score = questionAnwser.getScore();
        }
        return score;
    }

    @Override
    public TestSoulComment findSoulComment(Integer score) {
        Query query = null;
        TestSoulComment testSoulComment = null;
        if(score < 21) {
            query = new Query(new Criteria("id").is(1));
            testSoulComment = mongoTemplate.findOne(query, TestSoulComment.class);
        } else if(score < 40) {
            query = new Query(new Criteria("id").is(2));
            testSoulComment = mongoTemplate.findOne(query, TestSoulComment.class);
        } else if(score <= 55) {
            query = new Query(new Criteria("id").is(3));
            testSoulComment = mongoTemplate.findOne(query, TestSoulComment.class);
        } else {
            query = new Query(new Criteria("id").is(4));
            testSoulComment = mongoTemplate.findOne(query, TestSoulComment.class);
        }
        return testSoulComment;
    }

    @Override
    public String findQuestionLevel(String questionId) {
        Query query = new Query(new Criteria("questionId").is(new ObjectId(questionId)));
        Question question = mongoTemplate.findOne(query, Question.class);
        return question.getLevel();
    }

    @Override
    public List<TestSoulConclusion> findConclusionSimilar(Integer id, Long userId, String level) {
        Query query = new Query(new Criteria("type").is(id).and("userId").ne(userId).and("level").is(level));
        return mongoTemplate.find(query, TestSoulConclusion.class);
    }

    @Override
    public void saveTestSoulConclusion(TestSoulConclusion testSoulConclusion) {
        mongoTemplate.save(testSoulConclusion);
    }

    @Override
    public TestSoulConclusion findConclusionById(String reportId) {
        Query query = new Query(new Criteria("id").is(new ObjectId(reportId)));
        return mongoTemplate.findOne(query, TestSoulConclusion.class);
    }

    @Override
    public boolean findConclusion(Long userId, String level) {
        Query query = new Query(new Criteria("userId").is(userId).and("level").is(level));
        List<TestSoulConclusion> testSoulConclusionList = mongoTemplate.find(query, TestSoulConclusion.class);
        if(testSoulConclusionList.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public List<Question> findQuestionMiddle() {
        Query query = new Query(new Criteria("level").is("中级"));
        return mongoTemplate.find(query, Question.class);
    }

    @Override
    public List<Question> findQuestionHigh() {
        Query query = new Query(new Criteria("level").is("高级"));
        return mongoTemplate.find(query, Question.class);
    }
}
