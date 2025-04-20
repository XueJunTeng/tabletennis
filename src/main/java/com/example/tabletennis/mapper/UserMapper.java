package com.example.tabletennis.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.tabletennis.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    void insertUser(User user);
    User selectByEmail(String email);
    User selectByUsername(String username);
    User selectById(Long userId);
    void updateLastLoginTime(Long userId, LocalDateTime lastLoginTime);
    String selectUsernameById(Long userId);
    Optional<Long> selectUserIdByUsername(String username);
    // 新增方法：查询所有活跃用户
    List<User> selectAllActiveUsers();
    // 新增分页查询
    List<User> selectUsersByCondition(Map<String, Object> params);

    // 新增总数统计
    int countUsersByCondition(Map<String, Object> params);

    // 新增管理操作
    int updateUserStatus(@Param("userId") Long userId,
                         @Param("status") String status);

    int updateUserRole(@Param("userId") Long userId,
                       @Param("role") String role);

    int batchUpdateStatus(@Param("userIds") List<Long> userIds,
                          @Param("status") String status);

    int batchDeleteUsers(@Param("userIds") List<Long> userIds);

    int batchUpdateRoles(@Param("userIds") List<Long> userIds,
                         @Param("role") String role);

    boolean existsById(Long userId);

    Long selectIdByUsername(String username);

    void updateUserName(Long userId, String username);

    void updateEmail(Long userId,String email);

    String selectPasswordByUserId(Long userId);

    void updatePassword(Long userId, String newEncodedPassword);

    void updateAvatarUrl(Long userId, String avatarUrl);
}