package com.example.tabletennis.enums;

/**
 * 内容类型枚举
 * - 用于区分内容的类型（如视频、文章）
 */
public enum ContentType {
    VIDEO("VIDEO"),      // 视频
    ARTICLE("ARTICLE"),  // 文章
    POST("POST");
    private final String dbValue;

    ContentType(String dbValue) {
        this.dbValue = dbValue;
    }

    /**
     * 获取数据库存储值（如 "video"）
     */
    public String getDbValue() {
        return dbValue;
    }

    /**
     * 根据数据库值解析枚举
     * @throws IllegalArgumentException 如果数据库值无效
     */
    public static ContentType fromDbValue(String dbValue) {
        for (ContentType type : ContentType.values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的内容类型: " + dbValue);
    }
}