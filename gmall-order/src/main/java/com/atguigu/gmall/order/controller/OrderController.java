package com.atguigu.gmall.order.controller;

import com.alipay.api.AlipayApiException;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.order.config.AlipayTemplate;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import com.atguigu.gmall.order.vo.PayAsyncVo;
import com.atguigu.gmall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by FJC on 2019-11-16.
 */
@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AlipayTemplate alipayTemplate;

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

    /**
     * 提交订单返回订单id
     * @param orderSubmitVO
     * @return
     */
    @PostMapping("submit")
    public Resp<Object> submit(@RequestBody OrderSubmitVO orderSubmitVO){
        String form = null;
        try {
            OrderEntity orderEntity = this.orderService.submit(orderSubmitVO);
            PayVo payVo = new PayVo();
            payVo.setBody("商城支付系统");
            payVo.setOut_trade_no(orderEntity.getOrderSn());
            payVo.setSubject("支付平台");
            payVo.setTotal_amount(orderEntity.getTotalAmount().toString());
            form = this.alipayTemplate.pay(payVo);
            System.out.println(form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return Resp.ok(form);
    }

    @GetMapping("pay/success")
    public Resp<Object> psySuccess(PayAsyncVo payAsyncVo){

        //支付成功，修改订单状态
        orderService.paySuccess(payAsyncVo.getOut_trade_no());

        //减库存

        return Resp.ok(null);

    }

}






