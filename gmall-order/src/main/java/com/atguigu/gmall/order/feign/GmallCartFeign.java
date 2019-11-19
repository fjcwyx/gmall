package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.cart.api.GmallCartApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by FJC on 2019-11-16.
 */
@FeignClient("cart-service")
public interface GmallCartFeign extends GmallCartApi {
}
