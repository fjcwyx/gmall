package com.atguigu.gmall.cart.feign;

import com.atguigu.sms.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-service")
public interface GmallSmsFeign extends GmallSmsApi {
}