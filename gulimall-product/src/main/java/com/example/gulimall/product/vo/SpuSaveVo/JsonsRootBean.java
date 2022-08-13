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
public class JsonsRootBean {

    private String spuname;
    private String spudescription;
    private int catalogid;
    private int brandid;
    private double weight;
    private int publishstatus;
    private List<String> decript;
    private List<String> images;
    private Bounds bounds;
    private List<Baseattrs> baseattrs;
    private List<Skus> skus;
}