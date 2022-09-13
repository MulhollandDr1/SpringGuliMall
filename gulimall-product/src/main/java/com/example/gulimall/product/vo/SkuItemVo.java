package com.example.gulimall.product.vo;

import com.example.gulimall.product.entity.SkuImagesEntity;
import com.example.gulimall.product.entity.SkuInfoEntity;
import com.example.gulimall.product.entity.SpuImagesEntity;
import com.example.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {
    /*sku基本信息*/
    private SkuInfoEntity skuInfoEntity;
    /*sku图片信息*/
    private List<SkuImagesEntity> skuImagesEntityList;
    /*spu销售属性*/
    private List<SpuItemSaleAttrVo> skuItemSaleAttrVoList;
    /*spu介绍图片*/
    private List<SpuInfoDescEntity> spuInfoDescEntityList;
    /*spu规格参数*/
    private List<SkuItemBaseAttrVo> skuItemBaseAttrVoList;


}
