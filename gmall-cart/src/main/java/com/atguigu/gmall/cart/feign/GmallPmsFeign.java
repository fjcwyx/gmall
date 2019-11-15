package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.pms.feign.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by FJC on 2019-11-15.
 */
@FeignClient("pms-service")
public interface GmallPmsFeign extends GmallPmsApi {
}
