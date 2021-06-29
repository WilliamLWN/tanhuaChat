package com.tanhua.server.test;

import com.tanhua.commons.templates.AipFaceTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AipFaceTest {

    @Autowired
    private AipFaceTemplate template;

    @Test
    public void testSendMsg() throws IOException {
        String file = "/Users/williams/Desktop/1.jpg";
        File file1 = new File(file);
        byte[] bytes = Files.readAllBytes(file1.toPath());
        System.out.println(template.detect(bytes));
    }
}