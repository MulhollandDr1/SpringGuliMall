package com.example.gulimall.ware.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;


import com.example.gulimall.ware.vo.PurchaseDemandVo;
import com.example.gulimall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.gulimall.ware.entity.PurchaseEntity;
import com.example.gulimall.ware.service.PurchaseService;
import com.example.common.utils.PageUtils;
import com.example.common.utils.R;



/**
 * 采购信息
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2022-07-28 00:08:48
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 列表
     */
    @RequestMapping("/list")

    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")

    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")

    public R save(@RequestBody PurchaseEntity purchase){
        purchase.setCreateTime(new Date());
        purchase.setUpdateTime(new Date());
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")

    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")

    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }
    @GetMapping("unreceive/list")
    public R unreceiveList(@RequestParam Map<String,Object> params){
        PageUtils page = purchaseService.queryPageUnreceive(params);
        return R.ok().put("page",page);
    }
    @PostMapping("/merge")
    public R mergeDemand(@RequestBody PurchaseDemandVo purchaseDemandVo){
        purchaseService.mergeDemand(purchaseDemandVo);
        return R.ok();
    }
    @PostMapping("/received")
    public R receivedPurchaseOrder(@RequestBody Long[] purchaseIds){
        purchaseService.receivedPurchaseOrder(purchaseIds);
        return R.ok();
    }
    @PostMapping("/done")
    public R purchaseDone(@RequestBody PurchaseDoneVo purchaseDoneVo){
        purchaseService.purchaseDone(purchaseDoneVo);
        return R.ok();
    }
}
