package com.tanhua.server.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TimeTest {

    @Test
    public void test() {
//        获取明天0点时间
        Calendar cal=Calendar.getInstance();
        cal.add(Calendar.DATE,1);//这里改为1
        Date time=cal.getTime();
        long time1 = time.getTime();
        System.out.println(new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(time1));
    }
}
