package com.community.common.web;

/**
 * 认证拦截器与参数解析器之间的约定：拦截器解析出当前用户 id 后写入 request attribute，
 * {@link CurrentUserArgumentResolver} 再把它注入到 Controller 方法参数。
 */
public final class AuthAttributes {

    /** 当前登录用户 id（Long）；游客请求不存在该属性。 */
    public static final String CURRENT_USER_ID = "jianyu.auth.currentUserId";

    private AuthAttributes() {
    }
}
