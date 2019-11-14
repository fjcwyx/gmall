package com.atguigu.gmall.item.feign;

import com.atguigu.sms.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by FJC on 2019-11-14.
 */

@FeignClient("sms-service")
public interface GmallSmsFeign extends GmallSmsApi {
}
