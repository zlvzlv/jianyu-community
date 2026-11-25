package com.community.module.social.dto;

/**
 * 关注/粉丝列表里的用户卡片。
 */
public record UserBriefVO(
        Long userId,
        String username,
        String nickname,
        String avatarUrl,
        String bio,
        boolean isFollowing,
        boolean isMutual) {
}
