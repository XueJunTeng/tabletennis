package com.example.tabletennis.config;

import com.example.tabletennis.dto.ApiResponse;
import com.example.tabletennis.exception.BusinessException;
import com.example.tabletennis.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 处理所有运行时异常
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(
                ApiResponse.error(400, e.getMessage())
        );
    }

    // 处理其他未捕获异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        return ResponseEntity.internalServerError().body(
                ApiResponse.error(500, "服务器内部错误")
        );
    }
    @ExceptionHandler(MyBatisSystemException.class)
    public ResponseEntity<ApiResponse> handleMyBatisException(MyBatisSystemException ex) {
        Throwable rootCause = ExceptionUtils.getRootCause(ex);
        log.error("MyBatis 深层异常：", rootCause); // 确保打印完整堆栈
        return ResponseEntity.badRequest().body(ApiResponse.error("数据解析失败"));
    }

    // 专门处理参数校验异常
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String field = error.getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });
        return ApiResponse.error(400, "参数校验失败", errors);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponse response = new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @Data
    @AllArgsConstructor
    private static class ErrorResponse {
        private String code;
        private String message;
    }
}