package com.community.system;

/**
 * 【临时脚手架】最小用户视图。
 *
 * <p>正式表结构与字段以包 1 的 {@code sys_user} 为准（含 password_hash、status、created_at 等）。
 * 本记录只保留社交模块需要的展示字段。</p>
 */
public record SysUser(Long id, String username, String nickname, String avatarUrl, String bio, String status) {
}
