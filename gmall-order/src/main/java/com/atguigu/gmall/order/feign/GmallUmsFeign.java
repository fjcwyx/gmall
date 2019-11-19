package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by FJC on 2019-11-16.
 */
@FeignClient("ums-service")
public interface GmallUmsFeign extends GmallUmsApi {
}
