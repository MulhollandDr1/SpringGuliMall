package com.example.gulimall.search.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {
    public RestHighLevelClient esClient(){

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http")));
    }
}
