package com.alibaba.cloud.ai.dataagent.config;

import com.alibaba.cloud.ai.dataagent.interceptor.RateLimitInterceptor;
import com.alibaba.cloud.ai.dataagent.properties.FileStorageProperties;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.time.Duration;

@Configuration
@AllArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageProperties fileStorageProperties;
    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = Paths.get(fileStorageProperties.getPath()).toAbsolutePath().toString();

        registry.addResourceHandler(fileStorageProperties.getUrlPrefix() + "/**")
            .addResourceLocations("file:" + uploadDir + "/")
            .setCacheControl(CacheControl.maxAge(Duration.ofHours(1)));
    }
}
