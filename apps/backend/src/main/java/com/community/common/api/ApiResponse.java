package com.community.common.api;

/**
 * 统一响应体：{@code {code, message, data, traceId}}。
 *
 * <p>契约见 docs/design-proposal-v0.1.md 第 5.2 节。所有业务接口（文件流除外）都必须返回该结构，
 * 前端只判断 {@code code == 0}。</p>
 */
public record ApiResponse<T>(int code, String message, T data, String traceId) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "ok", data, com.community.common.web.TraceContext.currentTraceId());
    }

    public static ApiResponse<Void> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null, com.community.common.web.TraceContext.currentTraceId());
    }
}
