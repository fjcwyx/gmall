package com.atguigu.gmall.auth.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by FJC on 2019-11-14.
 */
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}
