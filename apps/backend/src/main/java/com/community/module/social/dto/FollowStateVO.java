package com.community.module.social.dto;

/**
 * 关注/取关后的最新状态，前端直接用它刷新按钮文案与粉丝数，不用再查一次主页。
 */
public record FollowStateVO(boolean isFollowing, boolean isMutual, long followerCount) {
}
