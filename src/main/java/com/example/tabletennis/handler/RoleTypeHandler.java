package com.example.tabletennis.handler;
import com.example.tabletennis.enums.Role;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.*;

/**
 * 用户角色枚举处理器
 */
public class RoleTypeHandler extends BaseTypeHandler<Role> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Role role, JdbcType jdbcType) throws SQLException {
        ps.setString(i, role.getDbValue()); // 存储为 "user" 或 "admin"
    }

    @Override
    public Role getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String dbValue = rs.getString(columnName);
        return Role.fromDbValue(dbValue);
    }

    @Override
    public Role getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String dbValue = rs.getString(columnIndex);
        return dbValue == null ? null : Role.fromDbValue(dbValue);
    }

    @Override
    public Role getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String dbValue = cs.getString(columnIndex);
        return dbValue == null ? null : Role.fromDbValue(dbValue);
    }
}