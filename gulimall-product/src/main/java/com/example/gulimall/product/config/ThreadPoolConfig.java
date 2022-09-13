package com.example.gulimall.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class ThreadPoolConfig {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolPropertiesConfig poolPropertiesConfig){
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(poolPropertiesConfig.getCorePoolSize(), poolPropertiesConfig.getMaximumPoolSize(), poolPropertiesConfig.getKeepAliveTime(), TimeUnit.SECONDS, new LinkedBlockingDeque<>(100000), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        return threadPoolExecutor;
    }
}
