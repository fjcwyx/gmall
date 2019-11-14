package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.wms.feign.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by FJC on 2019-11-14.
 */
@FeignClient("wms-service")
public interface GmallWmsFeign extends GmallWmsApi {
}
