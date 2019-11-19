package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.wms.feign.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by FJC on 2019-11-16.
 */
@FeignClient("wms-service")
public interface GmallWmsFeign extends GmallWmsApi {
}
