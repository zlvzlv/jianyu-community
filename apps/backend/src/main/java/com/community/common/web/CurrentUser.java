package com.community.common.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入当前登录用户。
 *
 * <p>用法：</p>
 * <pre>
 * // 必须登录：游客访问直接返回 20001
 * public ApiResponse&lt;?&gt; follow(@CurrentUser(required = true) CurrentUserInfo me, ...)
 * // 可选登录：游客访问时 me 为 null，用于计算「我是否已关注」这类个性化字段
 * public ApiResponse&lt;?&gt; profile(@CurrentUser CurrentUserInfo me, ...)
 * </pre>
 *
 * <p>正式权限体系（Spring Security + @PreAuthorize）由包 1 提供，本注解届时改为从
 * SecurityContext 取值即可，业务代码不用动。</p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {

    /** 是否强制要求登录。 */
    boolean required() default false;
}
