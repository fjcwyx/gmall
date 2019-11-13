package com.atguigu.gmall.index.annotation;

import java.lang.annotation.*;

/**
 * Created by FJC on 2019-11-13.
 */
@Target({ElementType.METHOD})       //注解作用方位
@Retention(RetentionPolicy.RUNTIME) //运行时注解
@Documented                         //加入文档
public @interface GmallCache {

    /**
     * 缓存key的前缀
     * @return
     */
    String prefix() default "cache";

    /**
     * 单位是秒
     * @return
     */
    long timeout() default 300l;

    /**
     * 为了防止缓存雪崩，设置的过期时间的随机方位
     * @return
     */
    long random() default 300l;
}
