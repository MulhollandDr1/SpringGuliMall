/**
  * Copyright 2022 bejson.com 
  */
package com.example.gulimall.product.vo.SpuSaveVo;
import lombok.Data;

import java.util.List;
import java.util.Date;

/**
 * Auto-generated: 2022-08-14 5:23:6
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class Skus {
    private List<Attr> attr;
    private Date skuName;
    private String price;
    private String skuTitle;
    private String skuSubtitle;
    private List<Images> images;
    private List<String> descar;
    private int fullCount;
    private double discount;
    private int countStatus;
    private int fullPrice;
    private int reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
}