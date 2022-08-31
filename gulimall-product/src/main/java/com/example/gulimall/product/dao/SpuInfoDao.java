package com.example.gulimall.product.dao;

import com.example.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu信息
*
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2022-07-27 15:38:52
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    void upStatus(@Param("spuId") Long spuId, @Param("code") Integer code);
}
