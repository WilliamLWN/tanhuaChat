package com.tanhua.commons.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tanhua.huanxin")
@Data
public class HuanXinProperties {

    private String url;
    private String orgName;
    private String appName;
    private String clientId;
    private String clientSecret;

    //封装成环信需要的数据结构格式
    public String getHuanXinUrl() {
        return this.url + this.orgName + "/" + this.appName;
    }
}
