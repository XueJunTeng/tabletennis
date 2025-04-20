// 修改后的 ApiResponse.java
package com.example.tabletennis.dto;

import com.example.tabletennis.entity.Notification;
import lombok.Data;

import java.util.List;

@Data
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 成功响应（保持原有方法）
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(200,message,null);
    }


    // 错误响应（新增带数据的版本）
    public static <T> ApiResponse<T> error(int code, String message, T errorData) {
        return new ApiResponse<>(code, message, errorData);
    }

    // 保持原有简单错误方法
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(400, message, null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }


}