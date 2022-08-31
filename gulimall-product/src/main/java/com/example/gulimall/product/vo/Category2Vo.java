package com.example.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Category2Vo {
    private String catalog1Id;
    private List<Category3Vo> catalog3List;
    private String id;
    private String name;
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Category3Vo{
        private String catalog2Id;
        private String id;
        private String name;
    }
}
