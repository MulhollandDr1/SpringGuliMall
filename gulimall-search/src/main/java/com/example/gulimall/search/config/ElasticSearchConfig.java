package com.example.gulimall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/*在容器中注入RestHighLevelClient,并保存一些请求选项设置*/
@Configuration
public class ElasticSearchConfig {
    /*每次发请求都需要拿到这个请求选项*/
    public static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
/*        builder.addHeader("Authorization", "Bearer " + TOKEN);
        builder.setHttpAsyncResponseConsumerFactory(
                new HttpAsyncResponseConsumerFactory
                        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));*/
        COMMON_OPTIONS = builder.build();
    }
/*esClient 用来发出请求，等等操作*/
    @Bean
    public RestHighLevelClient esClient() {
        RestHighLevelClient esClient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("192.168.248.128", 9200, "http")
                ));
        return esClient;
    }
}
