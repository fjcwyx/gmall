package com.atguigu.gmall.order.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by FJC on 2019-11-16.
 */
@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 跳转到订单详情页
     * @return
     */
    @GetMapping("confirm")
    public Resp<OrderConfirmVO> confirm(){
        OrderConfirmVO orderConfirmVO = this.orderService.confirm();
        return Resp.ok(orderConfirmVO);
    }

    public Resp<OrderConfirmVO> submit(){

        return null;
    }

}






