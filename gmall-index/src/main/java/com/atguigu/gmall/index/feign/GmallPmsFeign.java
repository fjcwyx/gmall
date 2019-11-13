package com.atguigu.gmall.index.feign;

import com.atguigu.gmall.pms.feign.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by FJC on 2019-11-13.
 */
@FeignClient("pms-service")
public interface GmallPmsFeign extends GmallPmsApi {
}
