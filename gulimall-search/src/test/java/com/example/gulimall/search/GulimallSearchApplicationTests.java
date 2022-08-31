package com.example.gulimall.search;

import com.example.gulimall.search.config.ElasticSearchConfig;
import lombok.Data;
import net.minidev.json.JSONValue;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class GulimallSearchApplicationTests {
    @Autowired
    RestHighLevelClient esClient;

    @Data
    static
    class User {
        private String name;
        private Integer age;
    }

    @Test
    void contextLoads() {
    }

    @Test
    void es() {
        System.out.println(esClient);
    }

    @Test
    void esIndex() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        User user = new User();
        user.setAge(17);
        user.setName("Jone");
        indexRequest.source(JSONValue.toJSONString(user),XContentType.JSON);
        IndexResponse indexResponse = esClient.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(indexResponse);
    }
    @Test
    void esQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("users");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = esClient.search(searchRequest,ElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(searchResponse);
    }
}
