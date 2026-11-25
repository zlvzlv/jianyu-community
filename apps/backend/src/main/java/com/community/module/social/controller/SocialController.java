package com.community.module.social.controller;

import com.community.common.api.ApiResponse;
import com.community.common.api.PageResult;
import com.community.common.web.CurrentUser;
import com.community.common.web.CurrentUserInfo;
import com.community.module.social.dto.FollowStateVO;
import com.community.module.social.dto.UserBriefVO;
import com.community.module.social.dto.UserProfileVO;
import com.community.module.social.service.SocialService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 社交与好友圈接口（包 5）。
 *
 * <p>路由前缀与契约见 docs/design-proposal-v0.1.md 第 5 节：</p>
 * <pre>
 * GET    /api/v1/users/{userId}/profile     个人主页聚合（游客可访问）
 * POST   /api/v1/users/{userId}/follow      关注（需登录）
 * DELETE /api/v1/users/{userId}/follow      取关（需登录）
 * GET    /api/v1/users/{userId}/followers   粉丝列表（游客可访问）
 * GET    /api/v1/users/{userId}/following   关注列表（游客可访问）
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/users")
public class SocialController {

    private final SocialService socialService;

    public SocialController(SocialService socialService) {
        this.socialService = socialService;
    }

    @GetMapping("/{userId}/profile")
    public ApiResponse<UserProfileVO> profile(@PathVariable long userId,
                                              @CurrentUser CurrentUserInfo me) {
        return ApiResponse.ok(socialService.getProfile(userId, me == null ? null : me.id()));
    }

    @PostMapping("/{userId}/follow")
    public ApiResponse<FollowStateVO> follow(@PathVariable long userId,
                                             @CurrentUser(required = true) CurrentUserInfo me) {
        return ApiResponse.ok(socialService.follow(me.id(), userId));
    }

    @DeleteMapping("/{userId}/follow")
    public ApiResponse<FollowStateVO> unfollow(@PathVariable long userId,
                                               @CurrentUser(required = true) CurrentUserInfo me) {
        return ApiResponse.ok(socialService.unfollow(me.id(), userId));
    }

    @GetMapping("/{userId}/followers")
    public ApiResponse<PageResult<UserBriefVO>> followers(@PathVariable long userId,
                                                          @RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "20") int pageSize,
                                                          @CurrentUser CurrentUserInfo me) {
        return ApiResponse.ok(socialService.listFollowers(userId, me == null ? null : me.id(), page, pageSize));
    }

    @GetMapping("/{userId}/following")
    public ApiResponse<PageResult<UserBriefVO>> following(@PathVariable long userId,
                                                          @RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "20") int pageSize,
                                                          @CurrentUser CurrentUserInfo me) {
        return ApiResponse.ok(socialService.listFollowing(userId, me == null ? null : me.id(), page, pageSize));
    }
}
