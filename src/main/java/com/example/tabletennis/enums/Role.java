package com.example.tabletennis.enums;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

/**
 * 用户角色枚举（实现 Spring Security 的 GrantedAuthority 接口）
 *
 * 功能说明：
 * 1. 定义系统中可用的用户角色（如 USER、ADMIN）。
 * 2. 实现 Spring Security 的权限标识生成逻辑。
 * 3. 提供数据库值与枚举之间的双向转换方法。
 */
@Getter
public enum Role implements GrantedAuthority {
    USER("USER"),    // 普通用户（数据库存储为 "user"，权限名为 ROLE_USER）
    ADMIN("ADMIN");  // 管理员（数据库存储为 "admin"，权限名为 ROLE_ADMIN）

    /**
     * -- GETTER --
     *  获取数据库存储值（如 "user"）
     */
    private final String dbValue;

    /**
     * 枚举构造函数
     * @param dbValue 数据库存储值（如 "user"）
     */
    Role(String dbValue) {
        this.dbValue = dbValue;
    }

    /**
     * 根据数据库存储值解析枚举实例
     * @param dbValue 数据库中的角色值（如 "user"）
     * @return 对应的 Role 枚举实例
     * @throws IllegalArgumentException 如果数据库值无效
     */
    public static Role fromDbValue(String dbValue) {
        for (Role role : Role.values()) {
            if (role.dbValue.equals(dbValue)) {
                return role;
            }
        }
        throw new IllegalArgumentException("无效的用户角色值: " + dbValue);
    }

    /**
     * 实现 Spring Security 的 GrantedAuthority 接口
     * @return 权限标识符（格式为 ROLE_XXX，如 ROLE_USER）
     */
    @Override
    public String getAuthority() {
        return "ROLE_" + this.name(); // 返回 ROLE_USER 或 ROLE_ADMIN
    }

}