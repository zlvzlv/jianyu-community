package com.community.module.social.spi;

import java.util.Optional;

/**
 * 社交模块查询用户信息的出口（端口）。
 *
 * <p>实现方：{@code com.community.system.SysUserDirectory}（临时）→ 包 1 的 SysUserService（正式）。
 * 这样社交模块只依赖接口，不依赖用户表。</p>
 */
public interface SocialUserDirectory {

    Optional<SocialUser> findById(long userId);
}
