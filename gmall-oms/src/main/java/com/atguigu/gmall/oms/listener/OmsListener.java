package com.atguigu.gmall.oms.listener;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.service.OrderService;
import com.atguigu.gmall.ums.vo.UserBoundVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
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
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ORDER-SUCCESS-QUEUE",durable = "true"),
            exchange = @Exchange(value = "GMALL-ORDER-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"order.pay"}
    ))
    public void paySuccess(String orderToken){
        //修改订单的状态
        if(this.orderService.success(orderToken) == 1){
            //解锁库存
            this.amqpTemplate.convertAndSend("WMS-EXCHANGE", "stock.minus",orderToken);
            //给用户增加积分
            OrderEntity orderEntity = this.orderService.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderToken));
            UserBoundVO userBoundVO = new UserBoundVO();
            userBoundVO.setUserId(orderEntity.getMemberId());
            userBoundVO.setGrouth(orderEntity.getGrowth());
            userBoundVO.setIntegration(orderEntity.getIntegration());
            this.amqpTemplate.convertAndSend("UMS-EXCHANGE", "user.bound", userBoundVO);
        }
    }
}
