package com.example.tabletennis.enums;

public enum ContentStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED");

    private final String dbValue;

    ContentStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    // 获取数据库存储值
    public String getDbValue() {
        return dbValue;
    }

    // 从数据库值反向解析枚举
    public static ContentStatus fromDbValue(String dbValue) {
        for (ContentStatus status : ContentStatus.values()) {
            if (status.dbValue.equals(dbValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的状态值: " + dbValue);
    }
}
