package com.example.gulimall.ware;

import com.example.common.to.es.SkuStockTo;
import com.example.gulimall.ware.dao.WareSkuDao;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class GulimallWareApplicationTests {
    @Autowired
    WareSkuDao wareSkuDao;
    @Test
    void skuSotck() {
        ArrayList<Long> longs = new ArrayList<>();
        longs.add(5L);
        longs.add(6L);
        List<SkuStockTo> skuStockTos = wareSkuDao.selectStockList(longs);
        System.out.println(skuStockTos);
    }
}
