package com.community.system;

import com.community.module.social.spi.SocialUser;
import com.community.module.social.spi.SocialUserDirectory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 把 {@link SysUserRepository} 适配成社交模块依赖的 {@link SocialUserDirectory}。
 *
 * <p>包 1 落地后只需把本类内部改成调用真正的 SysUserService，社交模块代码零改动。</p>
 */
@Component
public class SysUserDirectory implements SocialUserDirectory {

    private final SysUserRepository userRepository;

    public SysUserDirectory(SysUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<SocialUser> findById(long userId) {
        return userRepository.findById(userId).map(SysUserDirectory::toSocialUser);
    }

    private static SocialUser toSocialUser(SysUser user) {
        return new SocialUser(user.id(), user.username(), user.nickname(),
                user.avatarUrl(), user.bio(), user.status());
    }
}
