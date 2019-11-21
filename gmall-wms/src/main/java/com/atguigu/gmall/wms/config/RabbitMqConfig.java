package com.atguigu.gmall.wms.config;

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
        return new TopicExchange("WMS-EXCHANGE", true, false, null);
    }
    /**
     * 定义延迟队列
     * @return
     */
    @Bean
    public Queue queue(){
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "WMS-EXCHANGE");//交换机名称
        arguments.put("x-dead-letter-routing-key", "wms.ttl");//routing-key
        arguments.put("x-message-ttl", 60000);//消息的死亡时间
        return new Queue("WMS-TTL-QUEUE", true, false, false, arguments);
    }
    /**
     * 绑定，将交换机和队列绑定
     * @return
     */
    @Bean
    public Binding binding(){
        return new Binding("WMS-TTL-QUEUE", Binding.DestinationType.QUEUE, "WMS-EXCHANGE", "wms.unlock", null);
    }

    /**
     * 定义死信队列
     * @return
     */
    @Bean
    public Queue deadQueue(){
        //名称、持久化、不排他、自动删除、参数
        return new Queue("WMS-DEAD-QUEUE", true, false, false, null);
    }

    /**
     * 绑定死信队列
     * @return
     */
    @Bean
    public Binding deadBinding(){
        return new Binding("WMS-DEAD-QUEUE", Binding.DestinationType.QUEUE, "WMS-EXCHANGE", "wms.ttl", null);
    }
}