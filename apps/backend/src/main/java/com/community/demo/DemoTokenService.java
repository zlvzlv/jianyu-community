package com.community.demo;

import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 【临时脚手架 · 演示用】极简登录令牌。
 *
 * <p>令牌形如 {@code demo-3}，不含签名、不落库、不过期，仅供本周脱离包 1 底座做演示。
 * 正式实现由包 1 提供：JJWT 双 Token + Redis 黑名单 + 封禁即时踢下线。</p>
 */
@Component
public class DemoTokenService {

    private static final String PREFIX = "demo-";

    public String issue(long userId) {
        return PREFIX + userId;
    }

    public Optional<Long> resolve(String token) {
        if (token == null || !token.startsWith(PREFIX)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(token.substring(PREFIX.length())));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
