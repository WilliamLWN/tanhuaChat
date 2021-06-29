package com.tanhua.server.service;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Question;
import com.tanhua.domain.mongo.QuestionAnwser;
import com.tanhua.domain.mongo.TestSoulComment;
import com.tanhua.domain.mongo.TestSoulConclusion;
import com.tanhua.domain.vo.Answers;
import com.tanhua.domain.vo.QuestionVo;
import com.tanhua.domain.vo.QuestionnaireVo;
import com.tanhua.domain.vo.ReportVo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.SoulApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SoulService {
    @Reference
    private SoulApi soulApi;
    @Reference
    private UserInfoApi userInfoApi;

    /**
     * 接口名称：测灵魂-问卷列表
     *
     * @return
     */
    public ResponseEntity<Object> testSoul() {

        List<Question> questionListLow = soulApi.findQuestionLow();
        List<Question> questionListMiddle = soulApi.findQuestionMiddle();
        List<Question> questionListHigh = soulApi.findQuestionHigh();

        List<QuestionVo> questionVoListLow = getQuestionAndOption(questionListLow);
        List<QuestionVo> questionVoListMiddle = getQuestionAndOption(questionListMiddle);
        List<QuestionVo> questionVoListHigh = getQuestionAndOption(questionListHigh);

        List<QuestionnaireVo> testSoul;
        String level = "中级";
        boolean result;
        //如果从数据库查到了数据就返回true
        result = soulApi.findConclusion(UserHolder.getUserId(), level);
        if (result) {
            testSoul = getHighTestSoul(questionVoListLow, questionVoListMiddle, questionVoListHigh);
            return ResponseEntity.ok(testSoul);
        }
        level = "初级";
        result = soulApi.findConclusion(UserHolder.getUserId(), level);
        if (result) {
            testSoul = getMiddleTestSoul(questionVoListLow, questionVoListMiddle, questionVoListHigh);
            return ResponseEntity.ok(testSoul);
        }

        testSoul = getPrimaryTestSoul(questionVoListLow, questionVoListMiddle, questionVoListHigh);
        return ResponseEntity.ok(testSoul);
    }

    /**
     * 接口名称：测灵魂-提交问卷
     *
     * @return
     */
    public ResponseEntity<Object> sendTestSoul(Answers answerr) {
        Long userId = UserHolder.getUserId();
        UserInfo userInfo = userInfoApi.findById(userId);
        Integer score = 0;
        List<Map<String, String>> answers = answerr.getAnswers();
        for (Map<String, String> answer : answers) {
            String questionId = answer.get("questionId");
            String optionId = answer.get("optionId");

            score += soulApi.findQuestionAnwserScore(questionId, optionId);
        }
        System.out.println("score=" + score);
        TestSoulComment testSoulComment = soulApi.findSoulComment(score);
        String comment = testSoulComment.getComment();
        String cover = testSoulComment.getCover();
        List<Map<String, String>> dimensions = testSoulComment.getDimensions();

        TestSoulConclusion testSoulConclusion = new TestSoulConclusion();
        testSoulConclusion.setId(ObjectId.get());
        testSoulConclusion.setUserId(userId);
        testSoulConclusion.setGender(userInfo.getGender());
        String level = soulApi.findQuestionLevel(answers.get(0).get("questionId")); //查询第一条题的级别就好了，因为题目都是同一个级别的
        testSoulConclusion.setLevel(level);
        testSoulConclusion.setType(testSoulComment.getId());
        testSoulConclusion.setConclusion(comment);
        testSoulConclusion.setCover(cover);
        testSoulConclusion.setDimensions(dimensions);
        List<TestSoulConclusion> testSoulConclusionList = soulApi.findConclusionSimilar(testSoulComment.getId(), userId, level);
        List<Map<String, Object>> similarYou = new ArrayList<>();
        if (testSoulConclusionList != null) {
            for (TestSoulConclusion soulConclusion : testSoulConclusionList) {
                Long similarUserId = soulConclusion.getUserId();
                Map<String, Object> map = new HashMap<>();
                Integer userId1 = similarUserId.intValue();
                String avatar = userInfoApi.findById(similarUserId).getAvatar();
                map.put("id", userId1);
                map.put("avatar", avatar);
                similarYou.add(map);
            }
        }
        testSoulConclusion.setSimilarYou(similarYou);
        testSoulConclusion.setCreated(System.currentTimeMillis());

        soulApi.saveTestSoulConclusion(testSoulConclusion);
        return ResponseEntity.ok(testSoulConclusion.getId().toString());
    }

    /**
     * 接口名称：测灵魂-查看结果
     *
     * @return
     */
    public ResponseEntity<Object> report(String reportId) {
        TestSoulConclusion testSoulConclusion = soulApi.findConclusionById(reportId);
        ReportVo reportVo = null;
        if (testSoulConclusion != null) {
            reportVo = new ReportVo();
            reportVo.setConclusion(testSoulConclusion.getConclusion());
            reportVo.setCover(testSoulConclusion.getCover());
            reportVo.setDimensions(testSoulConclusion.getDimensions());
            reportVo.setSimilarYou(testSoulConclusion.getSimilarYou());
        }
        return ResponseEntity.ok(reportVo);
    }

    //初级灵魂题权限
    private List<QuestionnaireVo> getPrimaryTestSoul(List<QuestionVo> questionVoListLow, List<QuestionVo> questionVoListMiddle, List<QuestionVo> questionVoListHigh) {
        List<QuestionnaireVo> questionnaireVoList = new ArrayList<>();

        QuestionnaireVo questionnaireVo = new QuestionnaireVo();
        questionnaireVo.setId("1");
        questionnaireVo.setName("初级灵魂题");
        questionnaireVo.setCover("https://tanhua-dev.oss-cn-zhangjiakou.aliyuncs.com/images/test_soul/qn_cover_01.png");
        questionnaireVo.setLevel("初级");
        questionnaireVo.setStar(2);
        questionnaireVo.setQuestions(questionVoListLow);
        questionnaireVo.setIsLock(0);
//      questionnaireVo.setReportId(); //最新报告id
        questionnaireVoList.add(questionnaireVo);

        QuestionnaireVo questionnaireVo1 = new QuestionnaireVo();
        questionnaireVo1.setId("2");
        questionnaireVo1.setName("中级灵魂题");
        questionnaireVo1.setCover("https://tanhua-dev.oss-cn-zhangjiakou.aliyuncs.com/images/test_soul/qn_cover_02.png");
        questionnaireVo1.setLevel("中级");
        questionnaireVo1.setStar(3);
        questionnaireVo1.setQuestions(questionVoListMiddle);
        questionnaireVo1.setIsLock(1);  //先锁上
//      questionnaireVo1.setReportId(); //最新报告id
        questionnaireVoList.add(questionnaireVo1);

        QuestionnaireVo questionnaireVo2 = new QuestionnaireVo();
        questionnaireVo2.setId("2");
        questionnaireVo2.setName("高级灵魂题");
        questionnaireVo2.setCover("https://tanhua-dev.oss-cn-zhangjiakou.aliyuncs.com/images/test_soul/qn_cover_03.png");
        questionnaireVo2.setLevel("中级");
        questionnaireVo2.setStar(5);
        questionnaireVo2.setQuestions(questionVoListHigh);
        questionnaireVo2.setIsLock(1);  //先锁上
//      questionnaireVo2.setReportId(); //最新报告id
        questionnaireVoList.add(questionnaireVo2);

        return questionnaireVoList;
    }

    //中级灵魂题权限
    private List<QuestionnaireVo> getMiddleTestSoul(List<QuestionVo> questionVoListLow, List<QuestionVo> questionVoListMiddle, List<QuestionVo> questionVoListHigh) {
        List<QuestionnaireVo> questionnaireVoList = new ArrayList<>();

        QuestionnaireVo questionnaireVo = new QuestionnaireVo();
        questionnaireVo.setId("1");
        questionnaireVo.setName("初级灵魂题");
        questionnaireVo.setCover("https://tanhua-dev.oss-cn-zhangjiakou.aliyuncs.com/images/test_soul/qn_cover_01.png");
        questionnaireVo.setLevel("初级");
        questionnaireVo.setStar(2);
        questionnaireVo.setQuestions(questionVoListLow);
        questionnaireVo.setIsLock(0);
//      questionnaireVo.setReportId(); //最新报告id
        questionnaireVoList.add(questionnaireVo);

        QuestionnaireVo questionnaireVo1 = new QuestionnaireVo();
        questionnaireVo1.setId("2");
        questionnaireVo1.setName("中级灵魂题");
        questionnaireVo1.setCover("https://tanhua-dev.oss-cn-zhangjiakou.aliyuncs.com/images/test_soul/qn_cover_02.png");
        questionnaireVo1.setLevel("中级");
        questionnaireVo1.setStar(3);
        questionnaireVo1.setQuestions(questionVoListMiddle);
        questionnaireVo1.setIsLock(0);
//      questionnaireVo1.setReportId(); //最新报告id
        questionnaireVoList.add(questionnaireVo1);

        QuestionnaireVo questionnaireVo2 = new QuestionnaireVo();
        questionnaireVo2.setId("2");
        questionnaireVo2.setName("高级灵魂题");
        questionnaireVo2.setCover("https://tanhua-dev.oss-cn-zhangjiakou.aliyuncs.com/images/test_soul/qn_cover_03.png");
        questionnaireVo2.setLevel("中级");
        questionnaireVo2.setStar(5);
        questionnaireVo2.setQuestions(questionVoListHigh);
        questionnaireVo2.setIsLock(1);  //先锁上
//      questionnaireVo2.setReportId(); //最新报告id
        questionnaireVoList.add(questionnaireVo2);
        return questionnaireVoList;
    }

    //高级灵魂题权限
    private List<QuestionnaireVo> getHighTestSoul(List<QuestionVo> questionVoListLow, List<QuestionVo> questionVoListMiddle, List<QuestionVo> questionVoListHigh) {
        List<QuestionnaireVo> questionnaireVoList = new ArrayList<>();

        QuestionnaireVo questionnaireVo = new QuestionnaireVo();
        questionnaireVo.setId("1");
        questionnaireVo.setName("初级灵魂题");
        questionnaireVo.setCover("https://tanhua-dev.oss-cn-zhangjiakou.aliyuncs.com/images/test_soul/qn_cover_01.png");
        questionnaireVo.setLevel("初级");
        questionnaireVo.setStar(2);
        questionnaireVo.setQuestions(questionVoListLow);
        questionnaireVo.setIsLock(0);
//      questionnaireVo.setReportId(); //最新报告id
        questionnaireVoList.add(questionnaireVo);

        QuestionnaireVo questionnaireVo1 = new QuestionnaireVo();
        questionnaireVo1.setId("2");
        questionnaireVo1.setName("中级灵魂题");
        questionnaireVo1.setCover("https://tanhua-dev.oss-cn-zhangjiakou.aliyuncs.com/images/test_soul/qn_cover_02.png");
        questionnaireVo1.setLevel("中级");
        questionnaireVo1.setStar(3);
        questionnaireVo1.setQuestions(questionVoListMiddle);
        questionnaireVo1.setIsLock(0);
//      questionnaireVo1.setReportId(); //最新报告id
        questionnaireVoList.add(questionnaireVo1);

        QuestionnaireVo questionnaireVo2 = new QuestionnaireVo();
        questionnaireVo2.setId("2");
        questionnaireVo2.setName("高级灵魂题");
        questionnaireVo2.setCover("https://tanhua-dev.oss-cn-zhangjiakou.aliyuncs.com/images/test_soul/qn_cover_03.png");
        questionnaireVo2.setLevel("中级");
        questionnaireVo2.setStar(5);
        questionnaireVo2.setQuestions(questionVoListHigh);
        questionnaireVo2.setIsLock(0);
//      questionnaireVo2.setReportId(); //最新报告id
        questionnaireVoList.add(questionnaireVo2);

        return questionnaireVoList;
    }

    //给试题
    public List<QuestionVo> getQuestionAndOption(List<Question> questionList) {
        //试题
        List<QuestionVo> questionVoList = new ArrayList<>();
        for (Question question : questionList) {
            QuestionVo questionVo = new QuestionVo();
            questionVo.setId(question.getQuestionId().toString());
            questionVo.setQuestion(question.getQuestion());
            //每个问题的答案选项
            List<QuestionAnwser> questionAnwserList = soulApi.findQuestionAnwser(question.getQuestionId());
            //选项
            List<Map<String, String>> questionAnswerVoList = new ArrayList<>();
            for (QuestionAnwser questionAnwser : questionAnwserList) {
                Map<String, String> map = new HashMap<>();
                map.put("id", questionAnwser.getOptionId());
                map.put("option", questionAnwser.getOption());
                questionAnswerVoList.add(map);
            }
            questionVo.setOptions(questionAnswerVoList);
            questionVoList.add(questionVo);
        }
        return questionVoList;
    }
}
