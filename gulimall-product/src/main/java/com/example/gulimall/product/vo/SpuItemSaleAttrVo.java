package com.example.gulimall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class SpuItemSaleAttrVo {
    private Long attrId;
    private String attrName;
    private List<AttrValuesWithSkuIds> attrValuesWithSkuIdsList;
}
