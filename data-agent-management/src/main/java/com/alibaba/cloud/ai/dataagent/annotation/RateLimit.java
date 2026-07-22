package com.alibaba.cloud.ai.dataagent.annotation;

import java.lang.annotation.*;

/**
 * API 限流注解，基于 Redis 滑动窗口
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /** 时间窗口内最大请求数 */
    int maxRequests() default 60;
    /** 时间窗口（秒） */
    int windowSeconds() default 60;
    /** 限流提示 */
    String message() default "请求过于频繁，请稍后再试";
}
