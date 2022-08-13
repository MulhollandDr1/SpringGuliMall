package com.example.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.utils.PageUtils;
import com.example.gulimall.product.entity.AttrEntity;
import com.example.gulimall.product.entity.AttrGroupEntity;
import com.example.gulimall.product.vo.AttrGroupRelationVo;
import com.example.gulimall.product.vo.AttrGroupWithAttrVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2022-07-27 15:38:52
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageByCategoryId(Map<String, Object> params, Long categoryId);

    List<AttrEntity> getAttrRelation(Long attrgroupId);

    void deleteRelation(AttrGroupRelationVo[] attrGroupRelationVos);

    List<AttrGroupWithAttrVo> getAttrGroupWithAttr(Long catelogId);

    void removeAttrGroupAndRelation(Long[] attrGroupIds);
}

