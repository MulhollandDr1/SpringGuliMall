package com.example.gulimall.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@ConfigurationProperties(prefix = "gulimall.thread")
@Component
@Data
public class ThreadPoolPropertiesConfig {
    private Integer corePoolSize;
    private Integer maximumPoolSize;
    private Integer keepAliveTime;
}
