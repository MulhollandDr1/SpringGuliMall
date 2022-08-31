package com.example.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.example.common.constant.ElasticSearchConstant;
import com.example.common.to.es.SkuEsTo;
import com.example.gulimall.search.config.ElasticSearchConfig;
import com.example.gulimall.search.service.ElasticSearchSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ElasticSearchSaveServiceImpl implements ElasticSearchSaveService {
    @Autowired
    RestHighLevelClient esClient;
    @Override
    public boolean porductUp(List<SkuEsTo> skuEsToList) throws IOException {
        /*已经在kibana中用dsl创建了product映射*/
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsTo skuEsTo : skuEsToList) {
            bulkRequest.add(new IndexRequest(ElasticSearchConstant.PRODUCT_INDEX)
                    .id(skuEsTo.getSkuId().toString())
                    .source(JSON.toJSONString(skuEsTo), XContentType.JSON));/*JSON.toJSONString 需要引入fastjson依赖使用*/
        }
        BulkResponse bulkResponse = esClient.bulk(bulkRequest, ElasticSearchConfig.COMMON_OPTIONS);
        boolean hasFailures = bulkResponse.hasFailures();
        if(hasFailures){
            List<String> collect = Arrays.stream(bulkResponse.getItems()).map(
                    BulkItemResponse::getId
            ).collect(Collectors.toList());
            log.error("商品上架错误：{}",collect);
        }
        return hasFailures;
    }
}
