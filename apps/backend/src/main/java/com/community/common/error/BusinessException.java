package com.community.common.error;

/**
 * 业务异常：由 {@link GlobalExceptionHandler} 统一转成
 * {@code HTTP status + 统一响应体}，业务代码只抛不处理。
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
