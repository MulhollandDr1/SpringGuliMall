package com.example.gulimall.product.dao;

import com.example.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gulimall.product.entity.AttrGroupEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2022-07-27 15:38:52
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    void deleteBatchRelation(@Param("collects") List<AttrAttrgroupRelationEntity> collects);
}
