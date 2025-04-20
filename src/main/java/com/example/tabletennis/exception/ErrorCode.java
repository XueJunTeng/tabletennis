package com.example.tabletennis.exception;

public enum ErrorCode {
    NOTIFICATION_DELETE_FAILED("NOTI_001", "通知删除失败"),
    NOTIFICATION_NOT_FOUND("NOTI_002", "通知不存在");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}