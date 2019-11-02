package com.atguigu.gmall.pms.feign;

import com.atguigu.sms.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by FJC on 2019-11-01.
 */
@FeignClient("sms-service")
public interface GmallSmsFeign extends GmallSmsApi {
}
