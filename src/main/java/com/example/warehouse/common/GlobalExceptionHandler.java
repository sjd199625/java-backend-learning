package com.example.warehouse.common;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * 全局异常处理。
 *
 * 没有它时：Service 抛异常 → Tomcat 返回 HTML 错误页，前端 JSON.parse 会失败。
 * 有了它：任何异常都变成 Result JSON，前端可以统一用 message 做提示。
 *
 * @RestControllerAdvice 会作用于所有 @RestController。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(Result.failure(exception.getCode(), exception.getMessage()));
    }

    /**
     * @Valid @RequestBody 校验失败，例如名称为空、价格为负数。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.failure(ErrorCode.VALIDATION_ERROR, message));
    }

    /**
     * @Validated 校验 Query / Path 失败，例如 page=0。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(
            ConstraintViolationException exception
    ) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining("；"));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.failure(ErrorCode.VALIDATION_ERROR, message));
    }

    /**
     * JSON 格式错误，或字段类型对不上，例如 price 传了 "abc"。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleBadJson(HttpMessageNotReadableException exception) {
        log.warn("请求 JSON 无法解析: {}", exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.failure(ErrorCode.BAD_JSON, "请求 JSON 格式或字段类型不正确"));
    }

    /**
     * 路径参数类型错误，例如 /api/products/abc。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {
        String name = exception.getName();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.failure(ErrorCode.TYPE_MISMATCH, "参数类型错误: " + name));
    }

    /**
     * 未预料的异常只记完整日志，不把堆栈返回给前端，避免泄露内部信息。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnknownException(Exception exception) {
        log.error("系统异常", exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.failure(ErrorCode.INTERNAL_ERROR, "系统暂时不可用"));
    }
}
