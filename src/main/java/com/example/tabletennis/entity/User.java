package com.example.tabletennis.entity;

import com.example.tabletennis.enums.Role;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * 用户实体类（实现 Spring Security 的 UserDetails 接口）
 */
@Data
public class User implements UserDetails {
    private Long userId;                   // 用户ID
    private String username;                 // 用户名（唯一）
    private String password;                 // 密码（加密存储）
    private String email;                    // 邮箱（唯一）
    private String avatarUrl;                   //头像
    private String status;                  //账户状态
    private Role role = Role.USER;           // 用户角色（默认普通用户）
    private LocalDateTime registrationTime;  // 注册时间
    private LocalDateTime lastLoginTime;     // 最后登录时间
    // ------------------ Spring Security 接口方法 ------------------
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 返回用户角色（Role 枚举已实现 GrantedAuthority）
        return Collections.singletonList(role);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 账户永不过期
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 账户永不锁定
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 凭证永不过期
    }

    @Override
    public boolean isEnabled() {
        return true; // 账户始终启用
    }
}