package com.example.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.gulimall.product.vo.CategoryBrandRelationVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.gulimall.product.entity.CategoryBrandRelationEntity;
import com.example.gulimall.product.service.CategoryBrandRelationService;
import com.example.common.utils.PageUtils;
import com.example.common.utils.R;


/**
 * 品牌分类关联
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2022-07-27 15:38:52
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 列表
     */
    @RequestMapping("/catelog/list")

    public R list(@RequestParam Map<String, String> params) {
        Long brandId = Long.valueOf(params.get("brandId"));
        QueryWrapper<CategoryBrandRelationEntity> categoryBrandRelationEntityQueryWrapper = new QueryWrapper<>();
        categoryBrandRelationEntityQueryWrapper.eq("brand_id", brandId);
        List<CategoryBrandRelationEntity> list = categoryBrandRelationService.list(categoryBrandRelationEntityQueryWrapper);
        return R.ok().put("data", list);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")

    public R info(@PathVariable("id") Long id) {
        CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")

    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")

    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")

    public R delete(@RequestBody Long[] ids) {
        categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @GetMapping("/brands/list")
    public R relationBrandsList(@RequestParam("catId") Long catId) {
        List<CategoryBrandRelationEntity> brands = categoryBrandRelationService.getBrandsByCatId(catId);
        List<CategoryBrandRelationVo> collect = brands.stream().map(
                brand -> {
                    CategoryBrandRelationVo categoryBrandRelationVo = new CategoryBrandRelationVo();
                    BeanUtils.copyProperties(brand, categoryBrandRelationVo);
                    return categoryBrandRelationVo;
                }
        ).collect(Collectors.toList());
        return R.ok().put("data", collect);
    }
}
