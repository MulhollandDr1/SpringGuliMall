package com.example.gulimall.search.vo;

import com.example.common.to.es.SkuEsTo;
import lombok.Data;

import java.util.List;

@Data
public class SearchResultVo {
    private List<SkuEsTo> skuEsTos; /*从elasticsearch中查到的所有商品信息*/
    /*分页信息*/
    private Integer pageNum; /*当前页面*/
    private Long totalCount; /*总记录数*/
    private Integer pages; /*总页数*/
    private List<Integer> totalPages; /*总页数*/

    private List<CatalogVo> catalogVos;
    private List<BrandVo> brandVos;
    private List<AttrsVo> attrsVos;
    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }
    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }
    @Data
    public static class AttrsVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
}
