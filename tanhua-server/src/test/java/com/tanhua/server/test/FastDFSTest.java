package com.tanhua.server.test;

import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

//@SpringBootTest
//@RunWith(SpringRunner.class)
//public class FastDFSTest {
//
//    // 注入fastdfs文件上传客户端对象
//    @Autowired
//    private FastFileStorageClient storageClient;
//    // 注入web服务器对象, 主要用来获取上传地址
//    @Autowired
//    private FdfsWebServer fdfsWebServer;
//
//    @Test
//    public void upload() throws IOException {
//        String path = "/Users/williams/Desktop/image.jpg";
//        File file = new File(path);
//
//        // 文件上传到fastdfs服务器, 返回存储地址
//        StorePath storePath = storageClient.uploadFile(
//                FileUtils.openInputStream(file), file.length(), "jpg", null);
//        // group1/M00/00/00/wKgMoGBBiAiAWJWhAAHnjh7KpOc972.jpg
//        System.out.println("上传到storage服务器的目录地址：" + storePath.getFullPath());
//
//        // 得到完整的访问地址
//        // http://192.168.12.160:8888/group1/M00/00/00/wKgMoGBBiAiAWJWhAAHnjh7KpOc972.jpg
//        String url = fdfsWebServer.getWebServerUrl() + storePath.getFullPath();
//
//        System.out.println("url = " + url);
//    }
//}
