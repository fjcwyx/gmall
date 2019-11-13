package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.annotation.GmallCache;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by FJC on 2019-11-13.
 * 定义的切面类，拦截自定义注解，进行缓存和分布式锁的嵌入
 * 解耦
 */
@Aspect //声明为切面类
@Component //注入spring容器
public class GmallCacheAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 1.方法的返回值是object
     * 2.方法的参数ProceedingJoinPoint
     * 3.方法必须抛出Throwable异常
     * 4.通过joinpoint.proceed(args)执行原始方法
     */
    @Around("@annotation(com.atguigu.gmall.index.annotation.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable{

        //获取注解，获取参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//获取方法签名
        GmallCache annotation = signature.getMethod().getAnnotation(GmallCache.class); //获取注解对象
        Class returnType = signature.getReturnType(); //获取方法的返回值类型
        String prefix = annotation.prefix(); //获取缓存的前缀
        String args = Arrays.asList(joinPoint.getArgs()).toString(); //获取方法的参数

        String key = prefix + ":" + args;

        //查询缓存
        Object result = this.cacheHit(key, returnType);
        //如果有，直接返回
        if (result != null) {
            return result;
        }
        //如果缓存中没有数据，则加入分布式锁
        RLock lock = this.redissonClient.getLock("lock" + args);
        lock.lock();
        //查询缓存
        result = this.cacheHit(key, returnType);
            //如果有，直接返回
        if (result != null) {
            lock.unlock();
            return result;
        }
        //放入缓存,释放分布式锁
        result = joinPoint.proceed(joinPoint.getArgs());
        long timeout = annotation.timeout();
        timeout = timeout + (long) Math.random()*annotation.random();
        this.redisTemplate.opsForValue().set(key, JSON.toJSONString(result),timeout, TimeUnit.SECONDS);
        lock.unlock();
        return result;
    }
    private Object cacheHit(String key, Class returnType){

        String jsonString = this.redisTemplate.opsForValue().get(key);
        // 如果缓存中有，直接返回
        if (StringUtils.isNotBlank(jsonString)){
            return JSON.parseObject(jsonString, returnType);
        }
        return null;
    }
}











