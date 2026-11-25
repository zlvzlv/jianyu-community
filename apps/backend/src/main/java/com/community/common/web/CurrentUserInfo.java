package com.community.common.web;

/**
 * 当前登录用户的轻量视图。本模块只需要 id；昵称头像等展示字段由社交模块按 id 查询。
 */
public record CurrentUserInfo(Long id) {
}
