package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.oms.api.GmallOmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by FJC on 2019-11-21.
 */
@FeignClient("oms-service")
public interface GmallOmsFeign extends GmallOmsApi {
}
