package com.atguigu.gmall.oms.listener;

import com.atguigu.gmall.oms.service.OrderService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by FJC on 2019-11-21.
 */
@Component
public class OmsListener {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AmqpTemplate amqpTemplate;


    @RabbitListener(queues = {"OMS-DEAD-QUEUE"})
    public void closeOrder(String orderToken){

        //修改订单的状态
        if(this.orderService.closeOrder(orderToken) == 1){
            //解锁库存
            this.amqpTemplate.convertAndSend("WMS-EXCHANGE", "wms.ttl",orderToken);
        }

    }
}
