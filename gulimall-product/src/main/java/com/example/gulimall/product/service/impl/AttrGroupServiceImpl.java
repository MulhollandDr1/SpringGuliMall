package com.example.gulimall.product.service.impl;

import com.example.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.example.gulimall.product.dao.AttrDao;
import com.example.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.example.gulimall.product.entity.AttrEntity;
import com.example.gulimall.product.service.AttrService;
import com.example.gulimall.product.vo.AttrGroupRelationVo;
import com.example.gulimall.product.vo.AttrGroupWithAttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.PageUtils;
import com.example.common.utils.Query;

import com.example.gulimall.product.dao.AttrGroupDao;
import com.example.gulimall.product.entity.AttrGroupEntity;
import com.example.gulimall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    AttrDao attrDao;
    @Autowired
    AttrService attrService;
    @Autowired
    AttrGroupDao attrGroupDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCategoryId(Map<String, Object> params, Long categoryId) {
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<AttrGroupEntity>();
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        if (categoryId != 0L) {
            queryWrapper.eq("catelog_id", categoryId);
        }
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<AttrEntity> getAttrRelation(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        List<Long> collect;
        if(relationEntities.size() != 0){
            collect = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        }else {
            return new ArrayList<>();
        }

        return attrDao.selectBatchIds(collect);
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] attrGroupRelationVos) {
        List<AttrAttrgroupRelationEntity> collects = Arrays.stream(attrGroupRelationVos).map(
                attrGroupRelationVo -> {
                    AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
                    BeanUtils.copyProperties(attrGroupRelationVo, attrAttrgroupRelationEntity);
                    return attrAttrgroupRelationEntity;
                }
        ).collect(Collectors.toList());
        System.out.println("collects : " + collects);
        attrAttrgroupRelationDao.deleteBatchRelation(collects);
    }

    @Override
    public List<AttrGroupWithAttrVo> getAttrGroupWithAttr(Long catelogId) {
        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<AttrGroupWithAttrVo> attrGroupWithAttrVos = new ArrayList<>();
        if (attrGroupEntities.size() != 0) {
            attrGroupWithAttrVos = attrGroupEntities.stream().map(
                    attrGroupEntity -> {
                        AttrGroupWithAttrVo attrGroupWithAttrVo = new AttrGroupWithAttrVo();
                        List<AttrEntity> attrRelation = this.getAttrRelation(attrGroupEntity.getAttrGroupId());
                        BeanUtils.copyProperties(attrGroupEntity, attrGroupWithAttrVo);
                        attrGroupWithAttrVo.setAttrs(attrRelation);
                        return attrGroupWithAttrVo;
                    }
            ).collect(Collectors.toList());
        }
        return attrGroupWithAttrVos;
    }

    @Override
    public void removeAttrGroupAndRelation(Long[] attrGroupIds) {
        this.removeByIds(Arrays.asList(attrGroupIds));
        attrAttrgroupRelationDao.delete(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id",Arrays.asList(attrGroupIds)));
    }
}