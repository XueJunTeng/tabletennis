package com.example.tabletennis.handler;

import com.example.tabletennis.enums.ContentType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.*;

/**
 * MyBatis 内容类型枚举处理器
 * 用于实现数据库字段与 ContentType 枚举的自动类型转换
 */
public class ContentTypeHandler extends BaseTypeHandler<ContentType> {

    /**
     * 将 Java 枚举值转换为数据库字段值
     * @param ps PreparedStatement 对象
     * @param i 参数位置索引
     * @param type ContentType 枚举实例
     * @param jdbcType JDBC 类型（可忽略）
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ContentType type, JdbcType jdbcType)
            throws SQLException {
        // 将枚举的数据库存储值写入 PreparedStatement
        ps.setString(i, type.getDbValue()); // 例如存储为 "video" 或 "article"
    }

    /**
     * 从 ResultSet 中获取数据库字段值并转换为枚举
     * @param rs ResultSet 结果集
     * @param columnName 数据库列名
     * @return 对应的 ContentType 枚举实例，如果数据库值为 null 则返回 null
     */
    @Override
    public ContentType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String dbValue = rs.getString(columnName);
        return convertToEnum(dbValue);
    }

    /**
     * 从 ResultSet 中获取数据库字段值并转换为枚举（按列索引）
     * @param rs ResultSet 结果集
     * @param columnIndex 列索引
     * @return 对应的 ContentType 枚举实例，如果数据库值为 null 则返回 null
     */
    @Override
    public ContentType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String dbValue = rs.getString(columnIndex);
        return convertToEnum(dbValue);
    }

    /**
     * 从 CallableStatement 中获取数据库字段值并转换为枚举
     * @param cs CallableStatement 对象
     * @param columnIndex 列索引
     * @return 对应的 ContentType 枚举实例，如果数据库值为 null 则返回 null
     */
    @Override
    public ContentType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String dbValue = cs.getString(columnIndex);
        return convertToEnum(dbValue);
    }

    /**
     * 将数据库字符串值转换为 ContentType 枚举
     * @param dbValue 数据库存储的字符串值（如 "video"）
     * @return 对应的枚举实例
     * @throws IllegalArgumentException 如果值不合法
     */
    private ContentType convertToEnum(String dbValue) {
        if (dbValue == null) {
            return null; // 允许数据库字段为 NULL
        }
        // 调用枚举的解析方法，若值无效会抛出 IllegalArgumentException
        return ContentType.fromDbValue(dbValue);
    }
}