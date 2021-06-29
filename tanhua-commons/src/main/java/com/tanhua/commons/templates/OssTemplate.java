package com.tanhua.commons.templates;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.tanhua.commons.properties.OssProperties;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class OssTemplate {

    private OssProperties properties;

    public OssTemplate(OssProperties properties) {
        this.properties = properties;
    }

    /**
     * 文件上传到阿里云OSS
     * @param fileName 上传文件名
     * @param inputStream 文件输入流
     * @return 图片的访问地址
     */
    public String upload(String fileName, InputStream inputStream) {

        // Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = properties.getEndpoint();
        // 阿里云主账号AccessKey拥有所有API的访问权限
        String accessKeyId = properties.getAccessKey();
        String accessKeySecret = properties.getSecret();
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        //获取上传后缀名
        String sufix = fileName.substring(fileName.lastIndexOf("."));
        /**
         * 文件名称：
         *   images/2020/10/31/uuid(格式)
         * 拼接图片名称如下：
         */
        String name = "images/"+new SimpleDateFormat("yyyy/MM/dd").format(new Date()) +"/"
                + UUID.randomUUID().toString() + sufix;
        ossClient.putObject(properties.getBucketName(), name, inputStream);
        // 关闭OSSClient。
        ossClient.shutdown();

        return properties.getUrl() + "/" + name;
    }

    /**
     * 文件上传到阿里云OSS
     * @param videoFileName 上传文件名
     * @param inputStream 文件输入流
     * @return 视频的访问地址
     */
    public String uploadFile(String videoFileName, InputStream inputStream) {

        // Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = properties.getEndpoint();
        // 阿里云主账号AccessKey拥有所有API的访问权限
        String accessKeyId = properties.getAccessKey();
        String accessKeySecret = properties.getSecret();
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        //获取上传后缀名
        String sufix = videoFileName.substring(videoFileName.lastIndexOf(".") + 1);
        /**
         * 文件名称：
         *   videos/2020/10/31/uuid(格式)
         * 拼接视频名称如下：
         */
        String name = "videos/"+new SimpleDateFormat("yyyy/MM/dd").format(new Date()) +"/"
                + UUID.randomUUID().toString() + sufix;
        ossClient.putObject(properties.getBucketName(), name, inputStream);
        // 关闭OSSClient。
        ossClient.shutdown();

        return properties.getUrl() + "/" + name;
    }

    /**
     * 文件上传到阿里云OSS
     * @param voiceFileName 上传文件名
     * @param inputStream 文件输入流
     * @return 音频的访问地址
     */
    public String uploadVoice(String voiceFileName, InputStream inputStream) {

        // Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = properties.getEndpoint();
        // 阿里云主账号AccessKey拥有所有API的访问权限
        String accessKeyId = properties.getAccessKey();
        String accessKeySecret = properties.getSecret();
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        //获取上传后缀名
        String sufix = voiceFileName.substring(voiceFileName.lastIndexOf(".") + 1);
        /**
         * 文件名称：
         *   voices/2020/10/31/uuid(格式)
         * 拼接视频名称如下：
         */
        String name = "voices/"+new SimpleDateFormat("yyyy/MM/dd").format(new Date()) +"/"
                + UUID.randomUUID().toString() + sufix;
        ossClient.putObject(properties.getBucketName(), name, inputStream);
        // 关闭OSSClient。
        ossClient.shutdown();

        return properties.getUrl() + "/" + name;
    }
}
