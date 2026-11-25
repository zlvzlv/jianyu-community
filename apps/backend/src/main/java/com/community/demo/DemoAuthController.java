package com.community.demo;

import com.community.common.api.ApiResponse;
import com.community.common.error.BusinessException;
import com.community.common.error.ErrorCode;
import com.community.common.web.CurrentUser;
import com.community.common.web.CurrentUserInfo;
import com.community.system.SysUser;
import com.community.system.SysUserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 【临时脚手架 · 演示用】免密码登录，仅用于本周演示「关注/取关 + 个人主页」。
 *
 * <p>正式接口（包 1）：{@code POST /api/v1/auth/login}、{@code GET /api/v1/auth/me}，
 * 返回值结构与错误码保持一致，前端只要换 URL 即可。</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
public class DemoAuthController {

    private final SysUserRepository userRepository;
    private final DemoTokenService tokenService;

    public DemoAuthController(SysUserRepository userRepository, DemoTokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @PostMapping("/demo-login")
    public ApiResponse<DemoLoginResponse> demoLogin(@Valid @RequestBody DemoLoginRequest request) {
        SysUser user = userRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        if (!"ACTIVE".equals(user.status())) {
            throw new BusinessException(ErrorCode.ACCOUNT_BANNED);
        }
        return ApiResponse.ok(new DemoLoginResponse(
                tokenService.issue(user.id()),
                user.id(),
                user.username(),
                user.nickname(),
                user.avatarUrl()));
    }

    @GetMapping("/me")
    public ApiResponse<DemoLoginResponse> me(@CurrentUser(required = true) CurrentUserInfo me) {
        SysUser user = userRepository.findById(me.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        return ApiResponse.ok(new DemoLoginResponse(
                tokenService.issue(user.id()),
                user.id(),
                user.username(),
                user.nickname(),
                user.avatarUrl()));
    }

    public record DemoLoginRequest(@NotBlank(message = "用户名不能为空") String username) {
    }

    public record DemoLoginResponse(String token, Long userId, String username, String nickname, String avatarUrl) {
    }
}
