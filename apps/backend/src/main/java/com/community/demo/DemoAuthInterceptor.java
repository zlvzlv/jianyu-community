package com.community.demo;

import com.community.common.web.AuthAttributes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 【临时脚手架 · 演示用】解析 {@code Authorization: Bearer demo-<id>}。
 *
 * <p>只做「识别当前用户」，不做拦截：需要登录的接口由 {@code @CurrentUser(required = true)}
 * 自行拒绝，游客访问公开接口不受影响。包 1 的 Spring Security 落地后整个类删除。</p>
 */
@Component
public class DemoAuthInterceptor implements HandlerInterceptor {

    private static final String BEARER = "Bearer ";

    private final DemoTokenService tokenService;

    public DemoAuthInterceptor(DemoTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith(BEARER)) {
            tokenService.resolve(authorization.substring(BEARER.length()).trim())
                    .ifPresent(userId -> request.setAttribute(AuthAttributes.CURRENT_USER_ID, userId));
        }
        return true;
    }
}
