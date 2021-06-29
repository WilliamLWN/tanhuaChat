package com.tanhua.dubbo.test;

import com.tanhua.domain.mongo.Question;
import com.tanhua.domain.mongo.QuestionAnwser;
import com.tanhua.domain.mongo.TestSoulComment;
import com.tanhua.dubbo.DubboServerApplication;
import com.tanhua.dubbo.api.mongo.SoulApi;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DubboServerApplication.class)
public class SoulTest {

    @Autowired
    private SoulApi soulApi;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void testQuestionData() {
        Question question = new Question();
        ObjectId objectId = ObjectId.get();
        System.out.println("objectId：" + objectId);
        question.setQuestionId(objectId);
        question.setQuestion("你经常梦到自己在");
        soulApi.saveQuestion(question);
    }

    @Test
    public void testQuestionAnwserData() {
        ObjectId objectId = new ObjectId("60a7449297b3fc3ec0a60668");
        QuestionAnwser questionAnwser = new QuestionAnwser();
        questionAnwser.setQuestionId(objectId);
        questionAnwser.setOptionId("A");
        questionAnwser.setOption("落下");
        soulApi.saveQuestionAnwser(questionAnwser);

        QuestionAnwser questionAnwser1 = new QuestionAnwser();
        questionAnwser1.setQuestionId(objectId);
        questionAnwser1.setOptionId("B");
        questionAnwser1.setOption("打架或挣扎");
        soulApi.saveQuestionAnwser(questionAnwser1);

        QuestionAnwser questionAnwser2 = new QuestionAnwser();
        questionAnwser2.setQuestionId(objectId);
        questionAnwser2.setOptionId("C");
        questionAnwser2.setOption("找东西或人");
        soulApi.saveQuestionAnwser(questionAnwser2);

        QuestionAnwser questionAnwser3 = new QuestionAnwser();
        questionAnwser3.setQuestionId(objectId);
        questionAnwser3.setOptionId("D");
        questionAnwser3.setOption("飞或漂浮");
        soulApi.saveQuestionAnwser(questionAnwser3);

        QuestionAnwser questionAnwser4 = new QuestionAnwser();
        questionAnwser4.setQuestionId(objectId);
        questionAnwser4.setOptionId("E");
        questionAnwser4.setOption("你平常不做梦");
        soulApi.saveQuestionAnwser(questionAnwser4);

        QuestionAnwser questionAnwser5 = new QuestionAnwser();
        questionAnwser5.setQuestionId(objectId);
        questionAnwser5.setOptionId("F");
        questionAnwser5.setOption("你的梦都是愉快的");
        soulApi.saveQuestionAnwser(questionAnwser5);
//
//        QuestionAnwser questionAnwser6 = new QuestionAnwser();
//        questionAnwser6.setQuestionId(objectId);
//        questionAnwser6.setOptionId("G");
//        questionAnwser6.setOption("棕色或灰色");
//        soulApi.saveQuestionAnwser(questionAnwser6);
    }

    @Test
    public void testCommentData() {
        TestSoulComment testSoulComment = new TestSoulComment();
        testSoulComment.setId(4);
        testSoulComment.setComment(
                "狮子型：性格为充满自信、竞争心强、主动且企图心强烈，是个有决断力的领导者。一般而言，狮子型的人胸怀大志，勇于冒险，看问题能够直指核心，并对目标全力以赴。他们在领导风格及决策上，强调权威与果断，擅长危机处理，此种性格最适合开创性与改革性的工作。"
        );
        testSoulComment.setCover("https://tanhua-dev.oss-cn-zhangjiakou.aliyuncs.com/images/test_soul/lion.png");
        List list = new ArrayList();
        Map<String,String> map = new HashMap<>();
        map.put("key","外向");
        map.put("value","97.3%");
        list.add(0,map);
        Map<String,String> map1 = new HashMap<>();
        map.put("key","判断");
        map.put("value","78.58%");
        list.add(1,map1);
        Map<String,String> map2 = new HashMap<>();
        map.put("key","抽象");
        map.put("value","77.81%");
        list.add(2,map2);
        Map<String,String> map3 = new HashMap<>();
        map.put("key","理性");
        map.put("value","79.96%");
        list.add(3,map3);
        testSoulComment.setDimensions(list);
        soulApi.saveComment(testSoulComment);
    }

    @Test
    public void test() {
        Question question = new Question();
        ObjectId objectId = ObjectId.get();
        question.setQuestionId(objectId);
        question.setQuestion("你经常梦到自己在");
        question.setLevel("高级");
        soulApi.saveQuestion(question);



        QuestionAnwser questionAnwser = new QuestionAnwser();
        questionAnwser.setQuestionId(objectId);
        questionAnwser.setOptionId("A");
        questionAnwser.setOption("落下");
        soulApi.saveQuestionAnwser(questionAnwser);

        QuestionAnwser questionAnwser1 = new QuestionAnwser();
        questionAnwser1.setQuestionId(objectId);
        questionAnwser1.setOptionId("B");
        questionAnwser1.setOption("打架或挣扎");
        soulApi.saveQuestionAnwser(questionAnwser1);

        QuestionAnwser questionAnwser2 = new QuestionAnwser();
        questionAnwser2.setQuestionId(objectId);
        questionAnwser2.setOptionId("C");
        questionAnwser2.setOption(" 找东西或人");
        soulApi.saveQuestionAnwser(questionAnwser2);

        QuestionAnwser questionAnwser3 = new QuestionAnwser();
        questionAnwser3.setQuestionId(objectId);
        questionAnwser3.setOptionId("D");
        questionAnwser3.setOption(" 飞或漂浮");
        soulApi.saveQuestionAnwser(questionAnwser3);

//        QuestionAnwser questionAnwser4 = new QuestionAnwser();
//        questionAnwser4.setQuestionId(objectId);
//        questionAnwser4.setOptionId("E");
//        questionAnwser4.setOption("玩着你的耳朵，摸着你的下巴或用手整理头发");
//        soulApi.saveQuestionAnwser(questionAnwser4);
//
//        QuestionAnwser questionAnwser5 = new QuestionAnwser();
//        questionAnwser5.setQuestionId(objectId);
//        questionAnwser5.setOptionId("F");
//        questionAnwser5.setOption("你的梦都是愉快的");
//        soulApi.saveQuestionAnwser(questionAnwser5);
//
//        QuestionAnwser questionAnwser6 = new QuestionAnwser();
//        questionAnwser6.setQuestionId(objectId);
//        questionAnwser6.setOptionId("G");
//        questionAnwser6.setOption("棕色或灰色");
//        soulApi.saveQuestionAnwser(questionAnwser6);
    }
}
