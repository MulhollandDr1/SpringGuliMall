package com.example.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.PageUtils;
import com.example.common.utils.Query;
import com.example.gulimall.product.config.ThreadPoolConfig;
import com.example.gulimall.product.dao.*;
import com.example.gulimall.product.entity.SkuImagesEntity;
import com.example.gulimall.product.entity.SkuInfoEntity;
import com.example.gulimall.product.entity.SpuInfoDescEntity;
import com.example.gulimall.product.service.SkuInfoService;
import com.example.gulimall.product.vo.SkuItemBaseAttrVo;
import com.example.gulimall.product.vo.SkuItemVo;
import com.example.gulimall.product.vo.SpuItemSaleAttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Autowired
    SkuImagesDao skuImagesDao;
    @Autowired
    SpuInfoDescDao spuInfoDescDao;
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        String key = (String) params.get("key");
        QueryWrapper<SkuInfoEntity> skuInfoEntityQueryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotEmpty(key)){
            skuInfoEntityQueryWrapper.and(
                    wrapper -> {
                        wrapper.eq("sku_id",key).or().like("sku_name",key);
                    }
            );
        }
        String catelogId = (String) params.get("catelogId");
        if(StringUtils.isNotEmpty(catelogId) && !StringUtils.equals(catelogId,"0")){
            skuInfoEntityQueryWrapper.eq("catalog_id",catelogId);
        }
        String brandId = (String) params.get("brandId");
        if(StringUtils.isNotEmpty(brandId) && !StringUtils.equals(brandId,"0")){
            skuInfoEntityQueryWrapper.eq("brand_id",brandId);
        }
        String min = (String) params.get("min");
        if(StringUtils.isNotEmpty(min)){
            skuInfoEntityQueryWrapper.ge("price",new BigDecimal(min)); /*price > min*/
        }
        String max = (String) params.get("max");
        BigDecimal bigDecimalMax = new BigDecimal(max);
        if(StringUtils.isNotEmpty(max) && bigDecimalMax.compareTo(new BigDecimal("0")) == 1){
            skuInfoEntityQueryWrapper.le("price",bigDecimalMax);
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                skuInfoEntityQueryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public SkuItemVo getSkuItemInfo(Long skuId) throws ExecutionException, InterruptedException {
/*        *//*sku基本信息*//*
        private SkuInfoEntity skuInfoEntity;
        *//*sku图片信息*//*
        private List<SkuImagesEntity> skuImagesEntityList;
        *//*spu销售属性*//*
        private List<SkuItemSaleAttrVo> skuItemSaleAttrVoList;
        *//*spu介绍图片*//*
        private List<SpuImagesEntity> spuImagesEntityList;
        *//*spu规格参数*//*
        private List<SkuItemBaseAttrVo> skuItemBaseAttrVoList;*/
        SkuItemVo skuItemVo = new SkuItemVo();
        /*sku基本信息*/
        CompletableFuture<SkuInfoEntity> skuInfoThread = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfoEntity = this.getById(skuId);
            skuItemVo.setSkuInfoEntity(skuInfoEntity);
            return skuInfoEntity;
        }, threadPoolExecutor);
        /*sku图片信息*/
        CompletableFuture<Void> skuImageThread = CompletableFuture.runAsync(() -> {
            skuItemVo.setSkuImagesEntityList(skuImagesDao.selectList(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId)));
        }, threadPoolExecutor);

        /*spu介绍图片*/
        CompletableFuture<Void> spuDescThread = skuInfoThread.thenAcceptAsync((result) -> {
            skuItemVo.setSpuInfoDescEntityList(spuInfoDescDao.selectList(new QueryWrapper<SpuInfoDescEntity>().eq("spu_id", result.getSpuId())));
        }, threadPoolExecutor);

        /*spu销售属性*/
        CompletableFuture<Void> spuSaleThread = skuInfoThread.thenAcceptAsync((result) -> {
            List<SpuItemSaleAttrVo> spuItemSaleAttrVoList = skuSaleAttrValueDao.getSaleAttrValueBySpuId(result.getSpuId());
            skuItemVo.setSkuItemSaleAttrVoList(spuItemSaleAttrVoList);
        }, threadPoolExecutor);

        /*spu规格参数*/
        CompletableFuture<Void> spuAttrThread = skuInfoThread.thenAcceptAsync((result) -> {
            List<SkuItemBaseAttrVo> skuItemBaseAttrVoList = attrGroupDao.getAttrGroupAndAttrs(result.getSpuId(), result.getCatalogId());
            skuItemVo.setSkuItemBaseAttrVoList(skuItemBaseAttrVoList);
        }, threadPoolExecutor);

        CompletableFuture.allOf(skuImageThread,spuDescThread,spuSaleThread,spuAttrThread).get();
        System.out.println(skuItemVo);
        return skuItemVo;
    }

}