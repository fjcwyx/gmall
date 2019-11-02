package com.atguigu.sms;

import com.atguigu.core.bean.Resp;
import com.atguigu.sms.vo.SaleVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Created by FJC on 2019-11-01.
 */
public interface GmallSmsApi {
    @PostMapping("sms/skubounds/sale")
    public Resp<Object> saveSaleVo(@RequestBody SaleVO skuSaleVO);
}
