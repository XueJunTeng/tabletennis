package com.example.tabletennis.handler;
import com.example.tabletennis.enums.ContentStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.*;

/**
 * 内容状态枚举处理器
 */
public class ContentStatusTypeHandler extends BaseTypeHandler<ContentStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ContentStatus status, JdbcType jdbcType) throws SQLException {
        ps.setString(i, status.getDbValue());  // 将枚举值存储为字符串（如 "pending"）
    }

    @Override
    public ContentStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String dbValue = rs.getString(columnName);
        return dbValue == null ? null : ContentStatus.fromDbValue(dbValue);
    }

    @Override
    public ContentStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String dbValue = rs.getString(columnIndex);
        return dbValue == null ? null : ContentStatus.fromDbValue(dbValue);
    }

    @Override
    public ContentStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String dbValue = cs.getString(columnIndex);
        return dbValue == null ? null : ContentStatus.fromDbValue(dbValue);
    }
}