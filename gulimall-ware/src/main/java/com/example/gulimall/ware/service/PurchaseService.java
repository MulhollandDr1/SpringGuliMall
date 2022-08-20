package com.example.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.utils.PageUtils;
import com.example.gulimall.ware.entity.PurchaseEntity;
import com.example.gulimall.ware.vo.PurchaseDemandVo;
import com.example.gulimall.ware.vo.PurchaseDoneVo;

import java.util.Map;

/**
 * 采购信息
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2022-07-28 00:08:48
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceive(Map<String, Object> params);

    void mergeDemand(PurchaseDemandVo purchaseDemandVo);

    void receivedPurchaseOrder(Long[] purchaseIds);

    void purchaseDone(PurchaseDoneVo purchaseDoneVo);
}

