package com.example.gulimall.product.vo;

import lombok.Data;

@Data
public class ProductAttrVo {
    private Long attrId;
    /**
     * 属性名
     */
    private String attrName;
    /**
     * 属性值
     */
    private String attrValue;
    /**
     * 快速展示【是否展示在介绍上；0-否 1-是】
     */
    private Integer quickShow;
}
