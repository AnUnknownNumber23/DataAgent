package com.alibaba.cloud.ai.dataagent.interceptor;

import com.alibaba.cloud.ai.dataagent.annotation.RateLimit;
import com.alibaba.cloud.ai.dataagent.util.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }

        RateLimit annotation = method.getMethodAnnotation(RateLimit.class);
        if (annotation == null) {
            return true;
        }

        Long userId = CurrentUser.getUserId();
        String key = "rate:" + request.getRequestURI() + ":" + (userId != null ? userId : request.getRemoteAddr());
        int maxRequests = annotation.maxRequests();
        int windowSeconds = annotation.windowSeconds();

        Long current = redisTemplate.opsForValue().increment(key);
        if (current == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }

        if (current != null && current > maxRequests) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"" + annotation.message() + "\"}");
            return false;
        }

        return true;
    }
}
