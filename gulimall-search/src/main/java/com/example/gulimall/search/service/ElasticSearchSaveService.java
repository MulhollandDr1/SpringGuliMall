package com.example.gulimall.search.service;

import com.example.common.to.es.SkuEsTo;

import java.io.IOException;
import java.util.List;


public interface ElasticSearchSaveService {
    boolean porductUp(List<SkuEsTo> skuEsToList) throws IOException;
}
