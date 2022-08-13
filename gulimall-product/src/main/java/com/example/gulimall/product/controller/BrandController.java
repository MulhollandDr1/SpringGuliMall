package com.example.gulimall.product.controller;

import com.example.common.utils.PageUtils;
import com.example.common.utils.R;
import com.example.gulimall.product.entity.BrandEntity;
import com.example.gulimall.product.service.BrandService;
import com.example.gulimall.product.validator.AddGroup;
import com.example.gulimall.product.validator.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 品牌
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2022-07-27 15:38:52
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")

    public R list(@RequestParam Map<String, Object> params) {
//        PageUtils page = brandService.queryPage(params);
        PageUtils page = brandService.queryDetails(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")

    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
// @Valid 开启校验 , BindingResult 校验结果
    public R save(@Validated(value = {AddGroup.class}) @RequestBody BrandEntity brand/*, BindingResult bindingResult*/) {
        brandService.save(brand);
        //改为统一异常校验
/*        if (bindingResult.hasErrors()) {
            Map<String, String> map = new HashMap<>();
            // 获取校验错误结果
            bindingResult.getFieldErrors().forEach((error) -> {
                //提取校验错误消息和字段
                String defaultMessage = error.getDefaultMessage();
                String field = error.getField();
                map.put(field, defaultMessage);
            });
            return R.error(400, "提交的数据不合法").put("data", map);
        } else {
            brandService.save(brand);
        }*/
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")

    public R update(@Validated(value = {UpdateGroup.class}) @RequestBody BrandEntity brand) {
        brandService.updateDetail(brand);

        return R.ok();
    }

    @PostMapping("/update/status")
    public R updateStatus(@Validated(value = {UpdateGroup.class}) @RequestBody BrandEntity brand) {   //mybatis-plus默认不能更新逻辑删除状态，需要用自己写的sql来更新状态
        brandService.updateStatus(brand); //前端只需发送携带brandId和需要修改的信息即可,会自动把包含brand属性值的json转化为brandEntity
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")

    public R delete(@RequestBody Long[] brandIds) {
//        brandService.removeByIds(Arrays.asList(brandIds));
        brandService.removeBrandAndRelation(brandIds);

        return R.ok();
    }

}
