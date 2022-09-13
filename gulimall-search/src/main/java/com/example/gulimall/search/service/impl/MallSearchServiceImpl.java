package com.example.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.example.common.constant.ElasticSearchConstant;
import com.example.common.to.es.SkuEsTo;
import com.example.gulimall.search.config.ElasticSearchConfig;
import com.example.gulimall.search.service.MallSearchService;
import com.example.gulimall.search.vo.SearchParameterVo;
import com.example.gulimall.search.vo.SearchResultVo;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResultVo search(SearchParameterVo searchParameterVo) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        /*query*/
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.isNotEmpty(searchParameterVo.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", searchParameterVo.getKeyword()));
        }
        if (searchParameterVo.getCatalog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", searchParameterVo.getCatalog3Id()));
        }
        if (searchParameterVo.getBrandId() != null) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", searchParameterVo.getBrandId()));
        }
        if (CollectionUtils.isNotEmpty(searchParameterVo.getAttrs())) {
            for (String attr :
                    searchParameterVo.getAttrs()) {
                /*attrs=1_5寸:8寸&attrs2_16g:8g*/
                BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                String[] s = StringUtils.split(attr, "_");
                queryBuilder.must(QueryBuilders.termQuery("attrs.attrId", s[0]));
                String[] attrValues = StringUtils.split(s[1], ":");
                /*termsQuery检索的值可以直接用数组*/
                queryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                boolQueryBuilder.filter(QueryBuilders.nestedQuery("attrs", queryBuilder, ScoreMode.None));
            }
        }
        if (searchParameterVo.getHasStock() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", searchParameterVo.getHasStock() == 1));
        }
        if (StringUtils.isNotEmpty(searchParameterVo.getSkuPrice())) {
            /*1_500/_500/500_*/
            String[] price = StringUtils.split(searchParameterVo.getSkuPrice(), "_");
            if (price.length > 1) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("skuPrice").gte(price[0]).lte(price[1]));
            } else {
                if (StringUtils.startsWith(searchParameterVo.getSkuPrice(), "_")) {
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("skuPrice").gte(0).lte(price[0]));
                } else {
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("skuPrice").gte(price[0]).lte("9999999999"));
                }
            }
        }
        searchSourceBuilder.query(boolQueryBuilder);
        /*highlight*/
        if (StringUtils.isNotEmpty(searchParameterVo.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        /*sort*/
        if (StringUtils.isNotEmpty(searchParameterVo.getSort())) {
            /*sort:
             * saleCount_asc/desc
             * skuPrice_asc/desc
             * hotScore_asc/desc*/
            String[] sort = StringUtils.split(searchParameterVo.getSort(), "_");
            searchSourceBuilder.sort(sort[0], StringUtils.equalsIgnoreCase("asc", sort[1]) ? SortOrder.ASC : SortOrder.DESC);
        }
        /*page*/
        if (searchParameterVo.getPageNum() != null) {
            /*pageNum=1 : from 0,size 2 [0,1]
             * pageNum=2 : from 2,size 2 [2,3]
             * (pageNum - 1) * 5*/
            searchSourceBuilder.from((searchParameterVo.getPageNum() - 1) * ElasticSearchConstant.PRODUCT_PAGE_SIZE);
            searchSourceBuilder.size(ElasticSearchConstant.PRODUCT_PAGE_SIZE);
        }else {
            searchSourceBuilder.from(0);
            searchSourceBuilder.size(ElasticSearchConstant.PRODUCT_PAGE_SIZE);
        }
        /*aggregation*/
        /*brand aggregation*/
        TermsAggregationBuilder brandId_aggs = AggregationBuilders.terms("brandId_aggs").field("brandId");
        brandId_aggs.subAggregation(AggregationBuilders.terms("brandName_aggs").field("brandName"));
        brandId_aggs.subAggregation(AggregationBuilders.terms("brandImg_aggs").field("brandImg"));
        searchSourceBuilder.aggregation(brandId_aggs);
        /*catalog aggregation*/
        TermsAggregationBuilder catalogId_aggs = AggregationBuilders.terms("catalogId_aggs").field("catalogId").size(10);
        catalogId_aggs.subAggregation(AggregationBuilders.terms("catalogName_aggs").field("catalogName"));
        searchSourceBuilder.aggregation(catalogId_aggs);
        /*attrs aggregation*/
        NestedAggregationBuilder nestedAggregationBuilder = new NestedAggregationBuilder("attrs_nested_aggs", "attrs");
        TermsAggregationBuilder attrs_aggs = AggregationBuilders.terms("attrs_aggs").field("attrs.attrId");
        attrs_aggs.subAggregation(AggregationBuilders.terms("attrsName_aggs").field("attrs.attrName"));
        attrs_aggs.subAggregation(AggregationBuilders.terms("attrsValue_aggs").field("attrs.attrValue"));
        nestedAggregationBuilder.subAggregation(attrs_aggs);
        searchSourceBuilder.aggregation(nestedAggregationBuilder);
        SearchRequest searchRequest = new SearchRequest(new String[]{ElasticSearchConstant.PRODUCT_INDEX}, searchSourceBuilder);
        SearchResultVo searchResultVo = new SearchResultVo();
        try {
            /*发起请求*/
            SearchResponse response = restHighLevelClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
            /*解析响应数据*/
            SearchHits responseHits = response.getHits();
//          /*商品信息*/
            SearchHit[] hits = responseHits.getHits();
            List<SkuEsTo> skuEsToList = new ArrayList<>();
            if (ArrayUtils.isNotEmpty(hits)) {
                for (SearchHit hit :
                        hits) {
                    SkuEsTo skuEsTo = JSON.parseObject(hit.getSourceAsString(), new TypeReference<SkuEsTo>() {
                    });
                    /*高亮*/
                    if (StringUtils.isNotEmpty(searchParameterVo.getKeyword())) {
                        String skuTitle = hit.getHighlightFields().get("skuTitle").getFragments()[0].string();/*获取高亮区域，默认在高亮区域高亮keyword*/
                        skuEsTo.setSkuTitle(skuTitle);
                    }
                    skuEsToList.add(skuEsTo);
                }
                searchResultVo.setSkuEsTos(skuEsToList);
                /*分页信息*/
//            private Integer pageNum; 当前页面
//            private Long totalCount; 总记录数
//            private List<Integer> totalPages; 总页数
                searchResultVo.setPageNum(searchParameterVo.getPageNum());
                searchResultVo.setTotalCount(responseHits.getTotalHits().value);
                int pages = (int) responseHits.getTotalHits().value % ElasticSearchConstant.PRODUCT_PAGE_SIZE == 0 ? (int) responseHits.getTotalHits().value / 2 : (int) responseHits.getTotalHits().value / 2 + 1;
                searchResultVo.setPages(pages);
                ArrayList<Integer> totalPages = new ArrayList<>();
                for (int i = 1; i <= pages; i++){
                    totalPages.add(i);
                }
                searchResultVo.setTotalPages(totalPages);
                /*分类信息*/
                ParsedLongTerms catalogIdAggs = response.getAggregations().get("catalogId_aggs");/*此处需要修改得到的聚合类型为ParsedLongTerms*/
                List<? extends Terms.Bucket> buckets = catalogIdAggs.getBuckets();
                ArrayList<SearchResultVo.CatalogVo> catalogVos = new ArrayList<>();
                for (Terms.Bucket bucket :
                        buckets) {
                    SearchResultVo.CatalogVo catalogVo = new SearchResultVo.CatalogVo();
                    /*获取分类id*/
                    catalogVo.setCatalogId(Long.parseLong(bucket.getKeyAsString()));
                    /*获取子聚合中的分类名称*/
                    ParsedStringTerms catalogName_aggs = bucket.getAggregations().get("catalogName_aggs");
                    catalogVo.setCatalogName(catalogName_aggs.getBuckets().get(0).getKeyAsString());
                    catalogVos.add(catalogVo);
                }
                searchResultVo.setCatalogVos(catalogVos);
                /*品牌信息*/
                ParsedLongTerms brandIdAggs = response.getAggregations().get("brandId_aggs");
                List<? extends Terms.Bucket> brandIdAggsBuckets = brandIdAggs.getBuckets();
                ArrayList<SearchResultVo.BrandVo> brandVos = new ArrayList<>();
                for (Terms.Bucket bucket :
                        brandIdAggsBuckets) {
                    SearchResultVo.BrandVo brandVo = new SearchResultVo.BrandVo();
                    /*获取品牌id*/
                    brandVo.setBrandId(Long.parseLong(bucket.getKeyAsString()));
                    /*从子聚合中获取品牌名*/
                    ParsedStringTerms brandName_aggs = bucket.getAggregations().get("brandName_aggs");
                    brandVo.setBrandName(brandName_aggs.getBuckets().get(0).getKeyAsString());
                    /*从子聚合中获取品牌图片*/
                    ParsedStringTerms brandImg_aggs = bucket.getAggregations().get("brandImg_aggs");
                    brandVo.setBrandImg(brandImg_aggs.getBuckets().get(0).getKeyAsString());
                    brandVos.add(brandVo);
                }
                searchResultVo.setBrandVos(brandVos);
                /*属性信息*/
                ParsedNested attrs = response.getAggregations().get("attrs_nested_aggs");/*属性的聚合为嵌入式聚合*/
                ParsedLongTerms attrsAggs = attrs.getAggregations().get("attrs_aggs");
                List<? extends Terms.Bucket> attrsAggsBuckets = attrsAggs.getBuckets();
                ArrayList<SearchResultVo.AttrsVo> attrsVos = new ArrayList<>();
                for (Terms.Bucket bucket :
                        attrsAggsBuckets) {
                    SearchResultVo.AttrsVo attrsVo = new SearchResultVo.AttrsVo();
                    /*获取属性id*/
                    attrsVo.setAttrId(Long.parseLong(bucket.getKeyAsString()));
                    /*获取属性名*/
                    ParsedStringTerms attrsName_aggs = bucket.getAggregations().get("attrsName_aggs");
                    attrsVo.setAttrName(attrsName_aggs.getBuckets().get(0).getKeyAsString());
                    /*获取属性值（一个属性名可以有多个属性值）*/
                    ArrayList<String> attrValues = new ArrayList<>();
                    ParsedStringTerms attrsValue_aggs = bucket.getAggregations().get("attrsValue_aggs");
                    List<? extends Terms.Bucket> attrsValue_aggsBuckets = attrsValue_aggs.getBuckets();
                    for (Terms.Bucket attrsValueBucket :
                            attrsValue_aggsBuckets) {
                        attrValues.add(attrsValueBucket.getKeyAsString());
                    }
                    attrsVo.setAttrValue(attrValues);
                    attrsVos.add(attrsVo);
                }
                searchResultVo.setAttrsVos(attrsVos);
            }
            System.out.println(searchResultVo);
        } catch (Exception e) {
            System.out.println(e);
        }
        return searchResultVo;
    }
}
