package com.example.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.example.common.to.MemberPrice;
import com.example.common.to.SkuReductionTo;
import com.example.gulimall.coupon.dao.SkuLadderDao;
import com.example.gulimall.coupon.entity.MemberPriceEntity;
import com.example.gulimall.coupon.entity.SkuLadderEntity;
import com.example.gulimall.coupon.service.MemberPriceService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.PageUtils;
import com.example.common.utils.Query;

import com.example.gulimall.coupon.dao.SkuFullReductionDao;
import com.example.gulimall.coupon.entity.SkuFullReductionEntity;
import com.example.gulimall.coupon.service.SkuFullReductionService;
import org.springframework.transaction.annotation.Transactional;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {
    @Autowired
    SkuLadderDao skuLadderDao;
    @Autowired
    MemberPriceService memberPriceService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

                    return new PageUtils(page);
    }
    @Transactional
    @Override
    public void saveInfo(SkuReductionTo skuReductionTo) {
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        if(skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1 && skuReductionTo.getReducePrice().compareTo(new BigDecimal("0")) == 1){
            BeanUtils.copyProperties(skuReductionTo,skuFullReductionEntity);
            skuFullReductionEntity.setAddOther(skuReductionTo.getCountStatus());
            this.baseMapper.insert(skuFullReductionEntity);
        }
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        if(skuReductionTo.getFullCount() > 0 && skuReductionTo.getDiscount().compareTo(new BigDecimal("0")) == 1){
            BeanUtils.copyProperties(skuReductionTo,skuLadderEntity);
            skuLadderDao.insert(skuLadderEntity);
        }
        List<MemberPrice> memberPrices = skuReductionTo.getMemberPrice();
        if(CollectionUtils.isNotEmpty(memberPrices)){
            List<MemberPriceEntity> memberPriceEntityList = memberPrices.stream().map(
                    memberPrice -> {
                        MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                        memberPriceEntity.setSkuId(skuReductionTo.getSpuId());
                        memberPriceEntity.setMemberLevelId(memberPrice.getId());
                        memberPriceEntity.setMemberPrice(memberPrice.getPrice());
                        memberPriceEntity.setMemberLevelName(memberPrice.getName());
                        memberPriceEntity.setAddOther(1);
                        return memberPriceEntity;
                    }
            ).filter(memberPriceEntity -> {
                /*返回true就会把这条数据添加到集合中*/
                return memberPriceEntity.getMemberPrice().compareTo(new BigDecimal("0")) == 1;
            }).collect(Collectors.toList());
            memberPriceService.saveBatch(memberPriceEntityList);
        }
    }
}