package com.community.common.web;

import org.slf4j.MDC;

/**
 * 链路追踪号上下文：请求进入时由 {@link TraceIdFilter} 写入 MDC，
 * 统一响应体与日志都从这里取值，方便前端报错时直接贴 traceId 定位。
 */
public final class TraceContext {

    public static final String MDC_KEY = "traceId";
    static final String UNKNOWN = "unknown";

    private TraceContext() {
    }

    public static String currentTraceId() {
        String traceId = MDC.get(MDC_KEY);
        return traceId == null ? UNKNOWN : traceId;
    }
}
