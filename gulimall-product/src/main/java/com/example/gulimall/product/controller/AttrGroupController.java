package com.example.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.example.gulimall.product.entity.AttrEntity;
import com.example.gulimall.product.service.AttrAttrgroupRelationService;
import com.example.gulimall.product.service.AttrService;
import com.example.gulimall.product.service.CategoryService;
import com.example.gulimall.product.vo.AttrGroupRelationVo;
import com.example.gulimall.product.vo.AttrGroupWithAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.gulimall.product.entity.AttrGroupEntity;
import com.example.gulimall.product.service.AttrGroupService;
import com.example.common.utils.PageUtils;
import com.example.common.utils.R;



/**
 * 属性分组
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2022-07-27 15:38:52
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;
    /**
     * 列表
     */
    @GetMapping("/list/{catelogId}")

    public R list(@RequestParam Map<String, Object> params,@PathVariable("catelogId") Long categoryId){
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPageByCategoryId(params,categoryId);
        return R.ok().put("page", page);
    }
    @RequestMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        List<AttrEntity> attrEntities = attrGroupService.getAttrRelation(attrgroupId);
        return R.ok().put("data",attrEntities);
    }
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] attrGroupRelationVos){
        System.out.println("attrGroupVos : " + Arrays.toString(attrGroupRelationVos));
        attrGroupService.deleteRelation(attrGroupRelationVos);
        return R.ok();
    }
    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")

    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCategoryPath(catelogId);
        attrGroup.setCategoryPath(path);
        System.out.println("attrGroup : " + attrGroup);
        return R.ok().put("attrGroup", attrGroup);
    }
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@RequestParam Map<String,Object> params,
                            @PathVariable("attrgroupId") Long attrgroupId){
        PageUtils page = attrService.getAttrNoRelation(params,attrgroupId);
        return R.ok().put("page",page);
    }
    /**
     * 保存
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody AttrGroupRelationVo[] relationVos){
          attrAttrgroupRelationService.addBatchRelation(relationVos);
          return R.ok();
    }
    @GetMapping("{catelogId}/withattr")
    public R attrGroupWithAttr(@PathVariable("catelogId") Long catelogId){
        List<AttrGroupWithAttrVo> attrGroupWithAttrVos = attrGroupService.getAttrGroupWithAttr(catelogId);
        return R.ok().put("data",attrGroupWithAttrVos);
    }
    @RequestMapping("/save")

    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")

    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")

    public R delete(@RequestBody Long[] attrGroupIds){
//		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));
        attrGroupService.removeAttrGroupAndRelation(attrGroupIds);
        return R.ok();
    }

}
