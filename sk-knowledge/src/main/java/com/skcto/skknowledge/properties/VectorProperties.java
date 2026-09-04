package com.skcto.skknowledge.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 向量数据库属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "weaviate")
public class VectorProperties {

    private String protocol;
    private String host;
    private String apiKey;
}
