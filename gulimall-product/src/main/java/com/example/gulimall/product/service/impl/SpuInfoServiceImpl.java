package com.example.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.to.MemberPrice;
import com.example.common.to.SkuReductionTo;
import com.example.common.to.SpuBoundTo;
import com.example.common.utils.PageUtils;
import com.example.common.utils.Query;
import com.example.common.utils.R;
import com.example.gulimall.product.dao.AttrDao;
import com.example.gulimall.product.dao.SkuInfoDao;
import com.example.gulimall.product.dao.SpuInfoDao;
import com.example.gulimall.product.dao.SpuInfoDescDao;
import com.example.gulimall.product.entity.*;
import com.example.gulimall.product.feign.CouponFeignService;
import com.example.gulimall.product.service.*;
import com.example.gulimall.product.vo.SpuSaveVo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    SpuInfoDescDao spuInfoDescDao;
    @Autowired
    SpuImagesService spuImagesService;
    @Autowired
    AttrDao attrDao;
    @Autowired
    ProductAttrValueService productAttrValueService;
    @Autowired
    SkuInfoDao skuInfoDao;
    @Autowired
    SkuImagesService skuImagesService;;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    CouponFeignService couponFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        /*保存spu_info*/
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.baseMapper.insert(spuInfoEntity);
        /*保存pms_spu_info_desc*/
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", spuSaveVo.getDecript()));
        spuInfoDescDao.insert(spuInfoDescEntity);
        /*保存pms_spu_images，只保存img_url和spu_id两个属性*/
        List<String> images = spuSaveVo.getImages();
        if (images.size() != 0) {
            List<SpuImagesEntity> collect = images.stream().map(
                    image -> {
                        SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
                        spuImagesEntity.setSpuId(spuInfoEntity.getId());
                        spuImagesEntity.setImgUrl(image);
                        return spuImagesEntity;
                    }
            ).collect(Collectors.toList());
            spuImagesService.saveBatch(collect);
        }
        /*保存pms_product_attr_value*/
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(
                baseAttr -> {
                    ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                    productAttrValueEntity.setAttrValue(baseAttr.getAttrValues());
                    productAttrValueEntity.setAttrId(baseAttr.getAttrId());
                    productAttrValueEntity.setQuickShow(baseAttr.getShowDesc());
                    productAttrValueEntity.setSpuId(spuInfoEntity.getId());
                    productAttrValueEntity.setAttrName((attrDao.selectById(baseAttr.getAttrId()).getAttrName()));
                    return productAttrValueEntity;
                }
        ).collect(Collectors.toList());
        productAttrValueService.saveBatch(productAttrValueEntities);
        /*保存sms_spu_bounds*/
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(spuSaveVo.getBounds(),spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R spuR = couponFeignService.save(spuBoundTo);
        if(spuR.getCode() != 0){
            log.error("远程调用 couponFeignService ，保存spu信息失败");
        }
        /*保存pms_sku_info*/
        List<Skus> skus = spuSaveVo.getSkus();
        if (CollectionUtils.isNotEmpty(skus)) {
            for (Skus sku :
                    skus) {
                List<Images> skuImages = sku.getImages();
                String defaultImage = "";
                for (Images skuImage :
                        skuImages) {
                    if (skuImage.getDefaultImg() == 1) {
                        defaultImage = skuImage.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku,skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImage);
                skuInfoDao.insert(skuInfoEntity);
                /*保存pms_sku_images*/
                List<SkuImagesEntity> skuImagesEntities = skuImages.stream().map(
                        skuImage -> {
                            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                            BeanUtils.copyProperties(skuImage, skuImagesEntity);
                            skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
                            return skuImagesEntity;
                        }
                ).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntities);
                /*保存pms_sku_sale_attr_value*/
                List<Attr> skuAttrs = sku.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = skuAttrs.stream().map(
                        skuAttr -> {
                            SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                            BeanUtils.copyProperties(skuAttr, skuSaleAttrValueEntity);
                            skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                            return skuSaleAttrValueEntity;
                        }
                ).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);
                /*保存sms_sku_ladder和sms_sku_full_reduction*/
                if(sku.getFullCount() > 0 || sku.getFullPrice().compareTo(new BigDecimal("0")) == 1){
                    SkuReductionTo skuReductionTo = new SkuReductionTo();
                    BeanUtils.copyProperties(sku,skuReductionTo);
//                List<MemberPrice> memberPriceList = sku.getMemberPrice();
//                System.out.println("memberPriceList : "  + memberPriceList );
//                System.out.println("skuReductionTo.getMemberPrice() : " + skuReductionTo.getMemberPrice());
                    skuReductionTo.setSpuId(skuInfoEntity.getSkuId());
                    /*R skuR = */couponFeignService.saveSkuReduction(skuReductionTo);
/*                if(skuR.getCode() != 0){
                    log.error("远程调用 couponFeignService ，保存sku信息失败");
                }*/
                }
            }
        }
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> spuInfoEntityQueryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(StringUtils.isNotEmpty(key)){
            spuInfoEntityQueryWrapper.and(
                    w -> {
                        w.eq("id",key).or().like("spu_name",key);
                    }
            );
        }
        String catalogId = (String) params.get("catelogId");
        if(StringUtils.isNotEmpty(catalogId) && !StringUtils.equals(catalogId,"0")){
            spuInfoEntityQueryWrapper.eq("catalog_id",catalogId);
        }
        String brandId = (String) params.get("brandId");
        if(StringUtils.isNotEmpty(brandId) && !StringUtils.equals(brandId,"0")){
            spuInfoEntityQueryWrapper.eq("brand_id",brandId);
        }
        String status = (String) params.get("status");
        if(StringUtils.isNotEmpty(status)){
            spuInfoEntityQueryWrapper.eq("publish_status",status);
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                spuInfoEntityQueryWrapper
        );
        return new PageUtils(page);
    }

}