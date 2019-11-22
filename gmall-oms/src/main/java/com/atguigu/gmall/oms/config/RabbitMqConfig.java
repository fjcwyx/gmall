package com.atguigu.gmall.oms.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {

    /**
     * 定义交换机
     * @return
     */
    @Bean
    public Exchange exchange(){
        return new TopicExchange("OMS-EXCHANGE", true, false, null);
    }
    /**
     * 定义延迟队列
     * @return
     */
    @Bean
    public Queue queue(){
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "OMS-EXCHANGE");//交换机名称
        arguments.put("x-dead-letter-routing-key", "oms.dead");//routing-key
        arguments.put("x-message-ttl", 30000);//消息的死亡时间
        return new Queue("OMS-TTL-QUEUE", true, false, false, arguments);
    }
    /**
     * 绑定，将交换机和队列绑定
     * @return
     */
    @Bean
    public Binding binding(){
        return new Binding("OMS-TTL-QUEUE", Binding.DestinationType.QUEUE, "OMS-EXCHANGE", "oms.close", null);
    }

    /**
     * 定义死信队列
     * @return
     */
    @Bean
    public Queue deadQueue(){
        //名称、持久化、不排他、自动删除、参数
        return new Queue("OMS-DEAD-QUEUE", true, false, false, null);
    }

    /**
     * 绑定死信队列
     * @return
     */
    @Bean
    public Binding deadBinding(){
        return new Binding("OMS-DEAD-QUEUE", Binding.DestinationType.QUEUE, "OMS-EXCHANGE", "oms.dead", null);
    }
}