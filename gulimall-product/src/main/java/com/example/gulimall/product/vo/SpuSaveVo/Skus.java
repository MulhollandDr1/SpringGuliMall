package com.example.gulimall.product.vo.SpuSaveVo;

import lombok.Data;

import java.util.List;

/**
 * Auto-generated: 2022-08-14 4:53:21
 *
 * @author www.jsons.cn 
 * @website http://www.jsons.cn/json2java/ 
 */
@Data
public class Skus {

    private List<Attr> attr;
    private String skuname;
    private String price;
    private String skutitle;
    private String skusubtitle;
    private List<Images> images;
    private List<String> descar;
    private int fullcount;
    private double discount;
    private int countstatus;
    private int fullprice;
    private int reduceprice;
    private int pricestatus;
    private List<Memberprice> memberprice;

}