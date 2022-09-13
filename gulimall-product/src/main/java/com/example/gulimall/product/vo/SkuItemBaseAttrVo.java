package com.example.gulimall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class SkuItemBaseAttrVo {
    private String attrGroupName;
    private List<BaseAttrVo> baseAttrVoList;
}
