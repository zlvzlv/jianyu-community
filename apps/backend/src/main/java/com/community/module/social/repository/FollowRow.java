package com.community.module.social.repository;

/**
 * 关注/粉丝列表的查询结果行（已按「当前访问者」计算好关注状态）。
 */
public record FollowRow(
        Long userId,
        String username,
        String nickname,
        String avatarUrl,
        String bio,
        boolean following,
        boolean mutual) {
}
