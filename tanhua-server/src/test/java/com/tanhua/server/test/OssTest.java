package com.tanhua.server.test;

import com.tanhua.commons.templates.OssTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OssTest {

    @Autowired
    private OssTemplate ossTemplate;

    @Test
    public void testSendMsg() throws FileNotFoundException {
        String file = "/Users/williams/Desktop/image.jpg";
        String url = ossTemplate.upload(file, new FileInputStream(file));
        System.out.println("url = " + url);
    }
}