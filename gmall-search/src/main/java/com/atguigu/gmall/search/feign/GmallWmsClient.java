package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.wms.feign.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by FJC on 2019-11-10.
 */
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
