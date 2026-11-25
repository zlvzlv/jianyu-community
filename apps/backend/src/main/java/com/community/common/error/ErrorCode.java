package com.community.common.error;

/**
 * 业务错误码。
 *
 * <p>号段归属见 docs/design-proposal-v0.1.md 第 5.3 节，各模块按号段认领，禁止碰撞：</p>
 * <pre>
 * 0             成功
 * 10000-10999   公共参数与系统异常
 * 20000-20999   auth 账号与权限
 * 30000-30999   post / media
 * 40000-40999   interaction
 * 50000-50999   social（本模块）
 * 60000-60999   ai
 * 70000-70999   admin
 * </pre>
 */
public enum ErrorCode {

    SUCCESS(0, "ok", 200),

    // ── 公共 ──
    PARAM_INVALID(10001, "参数校验失败", 400),
    INTERNAL_ERROR(10500, "服务内部错误", 500),

    // ── auth（本周由临时 demo 登录使用，正式实现归包 1）──
    UNAUTHORIZED(20001, "未登录或 Token 已过期", 401),
    LOGIN_FAILED(20002, "账号或密码错误", 400),
    FORBIDDEN(20003, "无操作权限", 403),
    ACCOUNT_BANNED(20004, "账号已封禁", 403),

    // ── social（包 5）──
    CANNOT_FOLLOW_SELF(50001, "不能关注自己", 400),
    USER_NOT_FOUND(50002, "目标用户不存在", 404);

    private final int code;
    private final String message;
    private final int httpStatus;

    ErrorCode(int code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }

    public int httpStatus() {
        return httpStatus;
    }
}
