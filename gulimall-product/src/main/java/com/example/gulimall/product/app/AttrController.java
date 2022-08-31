package com.example.gulimall.product.app;

import java.util.List;
import java.util.Map;


import com.example.gulimall.product.entity.ProductAttrValueEntity;
import com.example.gulimall.product.service.ProductAttrValueService;
import com.example.gulimall.product.vo.AttrResponseVo;
import com.example.gulimall.product.vo.AttrVo;
import com.example.gulimall.product.vo.ProductAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.gulimall.product.service.AttrService;
import com.example.common.utils.PageUtils;
import com.example.common.utils.R;



/**
 * 商品属性
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2022-07-27 15:38:52
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    ProductAttrValueService productAttrValueService;
    /**
     * 列表
     */
    @RequestMapping("/{attrType}/list/{categoryId}")
    public R baseAttrList(@RequestParam Map<String,Object> params,
                          @PathVariable("categoryId") Long categoryId,
                          @PathVariable("attrType") String attrType
                          ){
        PageUtils page =  attrService.queryBaseAttrPage(params,categoryId,attrType);
        return R.ok().put("page",page);
    }
    @RequestMapping("/list")

    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")

    public R info(@PathVariable("attrId") Long attrId){
		AttrResponseVo attrResponseVo = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", attrResponseVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")

    public R save(@RequestBody AttrVo attrVo){
		attrService.saveAttr(attrVo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")

    public R update(@RequestBody AttrVo attrVo){
        attrService.updateAttr(attrVo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")

    public R delete(@RequestBody Long[] attrIds){
//		attrService.removeByIds(Arrays.asList(attrIds));
        attrService.removeAttrAndRelation(attrIds);

        return R.ok();
    }
    @GetMapping("/base/listforspu/{spuId}")
    public R listForSpu(@PathVariable("spuId") Long spuId){
        List<ProductAttrValueEntity> productAttrList = productAttrValueService.listForSpu(spuId);
        return R.ok().put("data",productAttrList);
    }
    @PostMapping("/update/{spuId}")
    public R updateProductAttr(@PathVariable("spuId") Long spuId, @RequestBody List<ProductAttrVo> productAttrVos){
        productAttrValueService.updateProductAttr(spuId,productAttrVos);
        return R.ok();
    }
}
