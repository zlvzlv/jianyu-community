package com.community.module.social.dto;

/**
 * 个人主页聚合视图。
 *
 * <p>{@code isSelf / isFollowing / isMutual} 都是相对「当前访问者」计算的：
 * 游客访问时三者均为 false；访问自己主页时 isSelf = true。</p>
 */
public record UserProfileVO(
        Long userId,
        String username,
        String nickname,
        String avatarUrl,
        String bio,
        long followerCount,
        long followingCount,
        boolean isSelf,
        boolean isFollowing,
        boolean isMutual) {
}
