package com.example.gulimall.search.controller;

import com.example.common.exception.BizCodeEnum;
import com.example.common.to.es.SkuEsTo;
import com.example.common.utils.R;
import com.example.gulimall.search.service.ElasticSearchSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
@Slf4j
@Controller
@RequestMapping("/elasticSearch/save")
public class ElasticSearchSaveController {
    @Autowired
    ElasticSearchSaveService elasticSearchSaveService;
    @ResponseBody
    @PostMapping("/product")
    public R productUp(@RequestBody List<SkuEsTo> skuEsToList){
        boolean b = true;  /*默认上架异常*/
        try {
            b = elasticSearchSaveService.porductUp(skuEsToList);
        }catch (Exception e){
            log.error("商品上架错误：{}",e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }
        if(b){
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }
        else {
            return R.ok();
        }
    }
}
