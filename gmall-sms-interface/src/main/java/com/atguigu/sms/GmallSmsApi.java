package com.atguigu.sms;

import com.atguigu.core.bean.Resp;
import com.atguigu.sms.vo.ItemSaleVO;
import com.atguigu.sms.vo.SaleVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Created by FJC on 2019-11-01.
 */
public interface GmallSmsApi {
    @PostMapping("sms/skubounds/sale")
    public Resp<Object> saveSaleVo(@RequestBody SaleVO skuSaleVO);

    @GetMapping("sms/skubounds/item/sales/{skuId}")
    public Resp<List<ItemSaleVO>> queryItemSaleVOs(@PathVariable("skuId") Long skuId);
}
