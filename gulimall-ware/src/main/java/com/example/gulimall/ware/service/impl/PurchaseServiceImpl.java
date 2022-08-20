package com.example.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.constant.WareConstant;
import com.example.common.constant.WareConstant.PurchaseEnum;
import com.example.common.utils.PageUtils;
import com.example.common.utils.Query;
import com.example.common.utils.R;
import com.example.gulimall.ware.dao.PurchaseDao;
import com.example.gulimall.ware.dao.WareSkuDao;
import com.example.gulimall.ware.entity.PurchaseDetailEntity;
import com.example.gulimall.ware.entity.PurchaseEntity;
import com.example.gulimall.ware.entity.WareSkuEntity;
import com.example.gulimall.ware.feign.ProductFeignService;
import com.example.gulimall.ware.service.PurchaseDetailService;
import com.example.gulimall.ware.service.PurchaseService;
import com.example.gulimall.ware.vo.PurchaseDemandVo;
import com.example.gulimall.ware.vo.PurchaseDoneItemVo;
import com.example.gulimall.ware.vo.PurchaseDoneVo;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {
    @Autowired
    PurchaseDetailService purchaseDetailService;
    @Autowired
    WareSkuDao wareSkuDao;
    @Autowired
    ProductFeignService productFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status",1).or().eq("status",0)
        );
        return new PageUtils(page);
    }
    @Transactional
    @Override
    public void mergeDemand(PurchaseDemandVo purchaseDemandVo) {
        Long purchaseId = purchaseDemandVo.getPurchaseId();
        if(purchaseId == null){
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        Long[] ids = purchaseDemandVo.getItems();  /*获取到的采购需求id，再通过id来对采购需求更新一些采购单信息*/
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = Arrays.stream(ids).map(
                id -> {
                    PurchaseDetailEntity purchaseDetailEntity = purchaseDetailService.getById(id);
                    purchaseDetailEntity.setPurchaseId(finalPurchaseId);
                    purchaseDetailEntity.setStatus(PurchaseEnum.ASSIGNED.getCode());
                    return purchaseDetailEntity;
                }
        ).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(collect);
        /*修改采购单更新时间*/
        PurchaseEntity purchaseEntity = this.getById(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }
    @Transactional
    @Override
    public void receivedPurchaseOrder(Long[] purchaseIds) {
        /*更新采购单状态和时间*/
        List<PurchaseEntity> purchaseEntityList = Arrays.stream(purchaseIds).map(
                this::getById
        ).filter(
                entity -> {
                    return entity.getStatus() == PurchaseEnum.CREATE.getCode() || entity.getStatus() == PurchaseEnum.ASSIGNED.getCode();
                }
        ).collect(Collectors.toList());
        List<PurchaseEntity> collect = purchaseEntityList.stream().peek(
                purchaseEntity -> {
                    purchaseEntity.setStatus(PurchaseEnum.RECEIVED.getCode());
                    purchaseEntity.setUpdateTime(new Date());
                }
        ).collect(Collectors.toList());
        this.updateBatchById(collect);
        /*更新采购项状态*/
        for (Long purchaseId :
                purchaseIds) {
            List<PurchaseDetailEntity> purchaseDetailEntityList = purchaseDetailService.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", purchaseId));
            List<PurchaseDetailEntity> detailEntityList = purchaseDetailEntityList.stream().peek(
                    purchaseDetailEntity -> purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailEnum.PURCHASING.getCode())
            ).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(detailEntityList);
        }
    }

    @Override
    public void purchaseDone(PurchaseDoneVo purchaseDoneVo) {
        /*更新采购项的状态以及更新库存sku信息*/
        Long purchaseId = purchaseDoneVo.getId();
        boolean flag = true;
        List<PurchaseDoneItemVo> items = purchaseDoneVo.getItems();
        ArrayList<PurchaseDetailEntity> purchaseDetailEntities = new ArrayList<>();
        for (PurchaseDoneItemVo item :
                items) {
            PurchaseDetailEntity purchaseDetailEntity = purchaseDetailService.getById(item.getItemId());
            purchaseDetailEntity.setStatus(item.getStatus());
            if(item.getStatus() == WareConstant.PurchaseDetailEnum.COMPLETED.getCode()){
                Integer count = wareSkuDao.selectCount(new QueryWrapper<WareSkuEntity>().eq("sku_id", purchaseDetailEntity.getSkuId()).eq("ware_id", purchaseDetailEntity.getWareId()));
                if (count == 0){ /*无库存记录就新增，否则更新*/
                    WareSkuEntity wareSkuEntity = new WareSkuEntity();
                    wareSkuEntity.setSkuId(purchaseDetailEntity.getSkuId());
                    wareSkuEntity.setWareId(purchaseDetailEntity.getWareId());
                    wareSkuEntity.setStock(purchaseDetailEntity.getSkuNum());
                    wareSkuEntity.setStock(0);
                    try{
                        R r = productFeignService.info(purchaseDetailEntity.getSkuId());
                        Map<String,Object> data = (Map<String, Object>) r.get("skuInfo");
                        if(r.getCode() == 0){
                            wareSkuEntity.setSkuName((String) data.get("skuName"));
                        }
                    }catch (Exception ignored){
                    }
                    wareSkuDao.insert(wareSkuEntity);
                }
                else {
                    wareSkuDao.updateStock(purchaseDetailEntity.getSkuId(),purchaseDetailEntity.getWareId(),purchaseDetailEntity.getSkuNum());
                }

            }
            else {
                flag = false;
            }
            purchaseDetailEntities.add(purchaseDetailEntity);
        }
        purchaseDetailService.updateBatchById(purchaseDetailEntities);
        /*通过所有采购项的状态来判断此采购单是否完成采购*/
        PurchaseEntity purchaseEntity = this.getById(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        purchaseEntity.setStatus(flag? PurchaseEnum.COMPLETED.getCode() : PurchaseEnum.UNUSUAL.getCode());
        this.updateById(purchaseEntity);
    }
}