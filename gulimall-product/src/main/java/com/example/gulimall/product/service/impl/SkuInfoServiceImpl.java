package com.example.gulimall.product.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.PageUtils;
import com.example.common.utils.Query;

import com.example.gulimall.product.dao.SkuInfoDao;
import com.example.gulimall.product.entity.SkuInfoEntity;
import com.example.gulimall.product.service.SkuInfoService;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

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

}