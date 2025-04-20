package com.example.tabletennis.enums;

/**
 * 用户行为类型枚举
 * - 用于记录用户在平台上的互动行为类型（如查看、点赞、收藏、评论）
 */
public enum BehaviorType {
    VIEW("VIEW"),        // 查看内容
    LIKE("LIKE"),        // 点赞
    FAVORITE("FAVORITE"),// 收藏
    COMMENT("COMMENT");  // 评论

    private final String dbValue;

    BehaviorType(String dbValue) {
        this.dbValue = dbValue;
    }

    /**
     * 获取数据库存储值（如 "view"）
     */
    public String getDbValue() {
        return dbValue;
    }

    /**
     * 根据数据库值解析枚举
     * @throws IllegalArgumentException 如果数据库值无效
     */
    public static BehaviorType fromDbValue(String dbValue) {
        for (BehaviorType type : BehaviorType.values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的用户行为类型: " + dbValue);
    }
}