package com.tanhua.commons.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tanhua.aip")
public class AipFaceProperties {

    private String appId;
    private String apiKey;
    private String secretKey;
}
