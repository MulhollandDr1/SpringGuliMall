/**
  * Copyright 2022 bejson.com 
  */
package com.example.gulimall.product.vo.SpuSaveVo;
import lombok.Data;

import java.util.Date;
import java.util.List;
/**
 * Auto-generated: 2022-08-14 5:23:6
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class JsonRootBean {
    private Date spuName;
    private Date spuDescription;
    private int catalogId;
    private int brandId;
    private double weight;
    private int publishStatus;
    private List<String> decript;
    private List<String> images;
    private Bounds bounds;
    private List<BaseAttrs> baseAttrs;
    private List<Skus> skus;
}