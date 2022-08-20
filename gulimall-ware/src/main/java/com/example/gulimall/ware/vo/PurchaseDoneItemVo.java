package com.example.gulimall.ware.vo;

import lombok.Data;

@Data
public class PurchaseDoneItemVo {
    private Long itemId;
    private int status;
    private String reason;
}
