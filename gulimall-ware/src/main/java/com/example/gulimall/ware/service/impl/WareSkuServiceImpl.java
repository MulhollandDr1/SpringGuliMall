package com.example.gulimall.ware.service.impl;

import com.example.common.to.es.SkuStockTo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.PageUtils;
import com.example.common.utils.Query;

import com.example.gulimall.ware.dao.WareSkuDao;
import com.example.gulimall.ware.entity.WareSkuEntity;
import com.example.gulimall.ware.service.WareSkuService;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        String skuId = (String) params.get("skuId");
        String wareId = (String) params.get("wareId");
        QueryWrapper<WareSkuEntity> wareSkuEntityQueryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotEmpty(skuId)){
            wareSkuEntityQueryWrapper.eq("sku_id",skuId);
        }
        if(StringUtils.isNotEmpty(wareId)){
            wareSkuEntityQueryWrapper.eq("ware_id",wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wareSkuEntityQueryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<SkuStockTo> hasStock(List<Long> skuIds) {
        List<SkuStockTo> stockList = baseMapper.selectStockList(skuIds); /*在mybatis的xml中创建resultMap，就可以将dao的多个返回值一一对应到SkuStockTo中*/
        return stockList;
    }

}