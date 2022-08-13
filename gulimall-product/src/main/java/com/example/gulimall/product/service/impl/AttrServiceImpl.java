package com.example.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.common.constant.ProductConstant;
import com.example.gulimall.product.dao.*;
import com.example.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.example.gulimall.product.entity.AttrGroupEntity;
import com.example.gulimall.product.service.CategoryService;
import com.example.gulimall.product.vo.AttrResponseVo;
import com.example.gulimall.product.vo.AttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.PageUtils;
import com.example.common.utils.Query;

import com.example.gulimall.product.entity.AttrEntity;
import com.example.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    CategoryDao categoryDao;
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attrVo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);
        this.save(attrEntity);
        if (attrVo.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long categoryId, String attrType) {
        QueryWrapper<AttrEntity> attrEntityQueryWrapper = new QueryWrapper<AttrEntity>().eq("attr_type", "base".equalsIgnoreCase(attrType) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        String key = (String) params.get("key");
        if (categoryId != 0L) {
            attrEntityQueryWrapper.eq("catelog_id", categoryId);
        }
        if (StringUtils.isNotEmpty(key)) {
            attrEntityQueryWrapper.and((obj) -> {
                obj.like("attr_name", key).or().eq("attr_id", key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                attrEntityQueryWrapper
        );
        List<AttrEntity> records = page.getRecords();
        List<AttrResponseVo> attrResponseVoList = records.stream().map((attrEntity) -> {
            AttrResponseVo attrResponseVo = new AttrResponseVo();
            BeanUtils.copyProperties(attrEntity, attrResponseVo);
            attrResponseVo.setCatelogName(categoryDao.selectById(attrEntity.getCatelogId()).getName());
            if (attrType.equals(ProductConstant.AttrEnum.ATTR_TYPE_BASE.getValue())) {
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));/*attr_id不会重复，属于自增字段*/
                if (attrAttrgroupRelationEntity != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                    attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            return attrResponseVo;
        }).collect(Collectors.toList());
        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(attrResponseVoList);
        return pageUtils;
    }

    @Override
    public AttrResponseVo getAttrInfo(Long attrId) {
        AttrEntity attrEntity = this.getById(attrId);
        AttrResponseVo attrResponseVo = new AttrResponseVo();
        BeanUtils.copyProperties(attrEntity, attrResponseVo);
        if (attrEntity.getAttrType().equals(ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode())) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (attrAttrgroupRelationEntity != null) {
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                attrResponseVo.setAttrGroupId(attrGroupEntity.getAttrGroupId());
            }
        }
        attrResponseVo.setCatelogPath(categoryService.findCategoryPath(attrEntity.getCatelogId()));
        return attrResponseVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attrVo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);
        this.updateById(attrEntity);
        if (attrEntity.getAttrType().equals(ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode())) {
            Integer count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            if (count > 0) {
                attrAttrgroupRelationDao.update(attrAttrgroupRelationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
            } else {
                attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
                attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
            }
        }
    }

    @Override
    public PageUtils getAttrNoRelation(Map<String, Object> params, Long attrgroupId) {
        /*1.通过分组id查找当前分类的所有属性*/
        /*2.在当前分类所有属性中排除已经被关联的属性，剩下没有被关联的属性就可以被关联*/
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>());
        QueryWrapper<AttrEntity> attrEntityQueryWrapper = new QueryWrapper<>();
        if (relationEntities.size() != 0) {
            List<Long> collect = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
            attrEntityQueryWrapper.notIn("attr_id", collect);
        }
        List<AttrEntity> attrEntityList = this.baseMapper.selectList(new QueryWrapper<AttrEntity>().eq("catelog_Id", catelogId));
//        System.out.println("attrEntityList.size() != 0 : " + (attrEntityList.size() != 0));
        if (attrEntityList.size() != 0) {
            attrEntityList = this.baseMapper.selectList(attrEntityQueryWrapper.eq("catelog_Id", catelogId));
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<>()
        );
        System.out.println("page.getTotal() : " + page.getTotal());
        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(attrEntityList);
        pageUtils.setTotalCount(attrEntityList.size());
//        System.out.println("pageUtils : " + pageUtils.getList());
        return pageUtils;
    }
    @Transactional
    @Override
    public void removeAttrAndRelation(Long[] attrIds) {
        System.out.println("attrIds : " + Arrays.toString(attrIds));
        this.removeByIds(Arrays.asList(attrIds));
        System.out.println("attrIds : " + Arrays.toString(attrIds));
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_id", attrIds));
        System.out.println("attrAttrgroupRelationEntities : " + attrAttrgroupRelationEntities);
        attrAttrgroupRelationDao.delete(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_id",Arrays.asList(attrIds)));
    }

}