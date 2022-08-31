package com.example.gulimall.ware.dao;

import com.example.common.to.es.SkuStockTo;
import com.example.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2022-07-28 00:08:48
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void updateStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    List<SkuStockTo> selectStockList(@Param("skuIds") List<Long> skuIds);
}
