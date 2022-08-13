package com.example.gulimall.product.vo.SpuSaveVo;

import lombok.Data;

/**
 * Auto-generated: 2022-08-14 4:53:21
 *
 * @author www.jsons.cn 
 * @website http://www.jsons.cn/json2java/ 
 */
@Data
public class Bounds {

    private int buybounds;
    private int growbounds;
    public void setBuybounds(int buybounds) {
         this.buybounds = buybounds;
     }
     public int getBuybounds() {
         return buybounds;
     }

    public void setGrowbounds(int growbounds) {
         this.growbounds = growbounds;
     }
     public int getGrowbounds() {
         return growbounds;
     }

}