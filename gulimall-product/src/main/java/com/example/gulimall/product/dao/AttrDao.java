package com.example.gulimall.product.dao;

import com.example.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * 商品属性
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2022-07-27 15:38:52
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    Set<Long> selectSearchAttrIds(@Param("spuAttrIds") Set<Long> spuAttrIdSet);
}
