package com.example.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.example.common.to.es.SkuStockTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.gulimall.ware.entity.WareSkuEntity;
import com.example.gulimall.ware.service.WareSkuService;
import com.example.common.utils.PageUtils;
import com.example.common.utils.R;



/**
 * 商品库存
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2022-07-28 00:08:48
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 列表
     */
    @RequestMapping("/list")

    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")

    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")

    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")

    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")

    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }
    @PostMapping("/hasStock")
    public R hasStock(@RequestBody List<Long> skuIds){
        List<SkuStockTo> skuStockTos = wareSkuService.hasStock(skuIds);
        return R.ok().setDate(skuStockTos);
    }
}
