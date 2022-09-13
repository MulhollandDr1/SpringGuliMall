package com.example.gulimall.search.vo;

import lombok.Data;

import java.util.List;
@Data
public class SearchParameterVo {
    private String keyword;
    private Long catalog3Id;
    /*sort:
    * saleCount_asc/desc
    * skuPrice_asc/desc
    * hotScore_asc/desc*/
    private String sort;
    private Integer hasStock;
    private String skuPrice;
    private List<Long> brandId;
    private List<String> attrs;
    private Integer pageNum;
}
