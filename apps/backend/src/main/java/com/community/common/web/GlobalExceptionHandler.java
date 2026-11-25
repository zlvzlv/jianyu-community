package com.community.common.web;

import com.community.common.api.ApiResponse;
import com.community.common.error.BusinessException;
import com.community.common.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理：所有异常统一转成 {@code {code, message, data, traceId}}，
 * 业务代码里不要再 try-catch 后手写响应体。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        ErrorCode errorCode = ex.errorCode();
        log.info("业务异常 code={} message={}", errorCode.code(), ex.getMessage());
        return ResponseEntity.status(errorCode.httpStatus())
                .body(ApiResponse.fail(errorCode.code(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidArgument(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError == null
                ? ErrorCode.PARAM_INVALID.message()
                : fieldError.getField() + " " + fieldError.getDefaultMessage();
        return ResponseEntity.status(ErrorCode.PARAM_INVALID.httpStatus())
                .body(ApiResponse.fail(ErrorCode.PARAM_INVALID.code(), message));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception ex) {
        return ResponseEntity.status(ErrorCode.PARAM_INVALID.httpStatus())
                .body(ApiResponse.fail(ErrorCode.PARAM_INVALID.code(), ErrorCode.PARAM_INVALID.message()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("未预期的服务异常", ex);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.httpStatus())
                .body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR.code(), ErrorCode.INTERNAL_ERROR.message()));
    }
}
