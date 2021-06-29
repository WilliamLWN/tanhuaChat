package com.tanhua.commons.config;

import com.tanhua.commons.properties.*;
import com.tanhua.commons.templates.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//自动的读取yml中配置信息，并赋值到SmsProperties、OssProperties对象中，将此对象存入容器
@EnableConfigurationProperties({
        SmsProperties.class,
        OssProperties.class,
        AipFaceProperties.class,
        HuanXinProperties.class,
        HuaWeiUGCProperties.class
})
public class TanhuaAutoConfiguration {

    @Bean
    public SmsTemplate smsTemplate(SmsProperties smsProperties) {
        SmsTemplate smsTemplate = new SmsTemplate(smsProperties);
        return smsTemplate;
    }

    @Bean
    public OssTemplate ossTemplate(OssProperties ossProperties) {
        OssTemplate ossTemplate = new OssTemplate(ossProperties);
        return ossTemplate;
    }

    @Bean
    public AipFaceTemplate aipFaceTemplate(AipFaceProperties aipFaceProperties) {
        AipFaceTemplate aipFaceTemplate = new AipFaceTemplate(aipFaceProperties);
        return aipFaceTemplate;
    }
    @Bean
    public HuanXinTemplate huanXinTemplate(HuanXinProperties properties) {
        return new HuanXinTemplate(properties);
    }
    @Bean
    public HuaWeiUGCTemplate huaWeiUGCTemplate(HuaWeiUGCProperties properties) {
        return new HuaWeiUGCTemplate(properties);
    }
}
