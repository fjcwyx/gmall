package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.pms.feign.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by FJC on 2019-11-10.
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
