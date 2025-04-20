package com.example.tabletennis.handler;

import com.example.tabletennis.enums.BehaviorType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.*;

public class BehaviorTypeHandler extends BaseTypeHandler<BehaviorType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, BehaviorType type, JdbcType jdbcType) throws SQLException {
        ps.setString(i, type.getDbValue()); // 存储为 "view"/"like" 等
    }

    @Override
    public BehaviorType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String dbValue = rs.getString(columnName);
        return dbValue == null ? null : BehaviorType.fromDbValue(dbValue);
    }

    @Override
    public BehaviorType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String dbValue = rs.getString(columnIndex);
        return dbValue == null ? null : BehaviorType.fromDbValue(dbValue);
    }

    @Override
    public BehaviorType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String dbValue = cs.getString(columnIndex);
        return dbValue == null ? null : BehaviorType.fromDbValue(dbValue);
    }
}