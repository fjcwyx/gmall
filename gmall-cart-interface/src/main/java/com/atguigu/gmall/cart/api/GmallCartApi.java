package com.atguigu.gmall.cart.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.vo.CartItemVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Created by FJC on 2019-11-18.
 */
public interface GmallCartApi{

    @GetMapping("cart/order/{userId}")
    public Resp<List<CartItemVO>> queryCartItemVO(@PathVariable("userId") Long userId);

}
