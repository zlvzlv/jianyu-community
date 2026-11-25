package com.community.module.social.spi;

/**
 * 社交模块需要的用户信息（只读视图）。
 *
 * <p>社交模块不直接依赖 sys_user 表，而是通过 {@link SocialUserDirectory} 拿数据：
 * 本周由临时脚手架实现，包 1 落地后替换实现即可。</p>
 */
public record SocialUser(Long id, String username, String nickname, String avatarUrl, String bio, String status) {

    public boolean active() {
        return "ACTIVE".equals(status);
    }
}
