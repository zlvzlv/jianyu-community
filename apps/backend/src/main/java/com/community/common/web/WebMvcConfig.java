package com.community.common.web;

import com.community.demo.DemoAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web 层装配：注册当前用户参数解析器 + 认证拦截器。
 *
 * <p>【临时】{@link DemoAuthInterceptor} 是本周演示用的简化登录（见 com.community.demo）。
 * 包 1 的 Spring Security + JWT 落地后，把这里替换成 SecurityFilterChain 即可，
 * {@code @CurrentUser} 的用法保持不变。</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final DemoAuthInterceptor demoAuthInterceptor;

    public WebMvcConfig(DemoAuthInterceptor demoAuthInterceptor) {
        this.demoAuthInterceptor = demoAuthInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserArgumentResolver());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(demoAuthInterceptor).addPathPatterns("/api/**");
    }
}
