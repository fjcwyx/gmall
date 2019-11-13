package com.atguigu.gmall.index.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

@Configuration
public class GmallJedisConfig {

    @Bean
    public JedisPool jedisPool(){

        return new JedisPool("192.168.79.129", 6379);
    }
}