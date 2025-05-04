package com.example.tabletennis.service;

import com.example.tabletennis.dto.PasswordChangeRequest;
import com.example.tabletennis.dto.ProfileUpdateRequest;
import com.example.tabletennis.dto.RegisterRequest;
import com.example.tabletennis.entity.User;
import com.example.tabletennis.dto.AuthRequest;
import com.example.tabletennis.enums.Role;
import com.example.tabletennis.mapper.UserMapper;
import com.example.tabletennis.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    // 修改注册方法返回User对象
    public User register(RegisterRequest request) {
        // 检查邮箱是否已存在
        if (userMapper.selectByEmail(request.getEmail()) != null) {
            throw new RuntimeException("邮箱已被注册");
        }
        // 检查用户名是否存在
        if (userMapper.selectByUsername(request.getUsername())!= null) {
            throw new RuntimeException("用户名已存在");
        }

        // 构建用户对象
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRegistrationTime(LocalDateTime.now());
        user.setRole(Role.USER); // 添加默认角色
        user.setAvatarUrl("http://localhost:8080/uploads/avatars/default-avatar.png"); // 添加默认头像

        // 插入数据库
        userMapper.insertUser(user);

        // 返回完整用户对象
        return userMapper.selectByUsername(user.getUsername());
    }

    // 登录方法返回User对象
    public User login(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();
            // 直接判断状态字段（假设字段名为status，禁用状态值为"disabled"）
            if ("disabled".equals(user.getStatus())) {
                throw new RuntimeException("账号已封禁");
            }
            userMapper.updateLastLoginTime(user.getUserId(), LocalDateTime.now());

            // 获取完整用户信息
            return userMapper.selectByUsername(user.getUsername());

        } catch (BadCredentialsException ex) {
            // 统一认证失败提示（避免信息泄露）
            throw new RuntimeException("用户名或密码错误");
        } catch (UsernameNotFoundException ex) {
            // 用户不存在提示（如果允许显示）
            throw new RuntimeException("用户不存在");
        } catch (Exception ex) {
            // 其他异常处理
            throw new RuntimeException(ex.getMessage());
        }
    }

    // 新增Token生成方法
    public String generateToken(User user) {
        return jwtUtils.generateToken(user);
    }

    public String getUsernameById(Long userId) {

        return userMapper.selectUsernameById(userId);
    }

    public Long getUserIdByUsername(String username) {
        return userMapper.selectUserIdByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
    }
    public User getUserById(Long userId) {

        return userMapper.selectById(userId);
    }


    public User updateUserProfile(Long userId, ProfileUpdateRequest request) {

        if(request.getUsername() != null) {
            userMapper.updateUserName(userId,request.getUsername());
        }
        else if (request.getEmail() != null) {
            userMapper.updateEmail(userId,request.getEmail());
        }
        return userMapper.selectById(userId);
    }

    public void changePassword(Long userId, PasswordChangeRequest request) {
        // 获取加密后的存储密码
        String storedEncodedPassword = userMapper.selectPasswordByUserId(userId);

        // 验证旧密码是否匹配
        if (!passwordEncoder.matches(request.getOldPassword(), storedEncodedPassword)) {
            throw new BadCredentialsException("旧密码错误");
        }

        // 加密新密码并更新
        String newEncodedPassword = passwordEncoder.encode(request.getNewPassword());
        userMapper.updatePassword(userId, newEncodedPassword);
    }

    public String uploadAvatar(Long userId, MultipartFile file) {
        // 从配置中获取参数（需要类上添加@Value注解）
        final String baseUrl = "http://localhost:8080";      // 实际用@Value("${app.base-url}")
        final String uploadDir = "D:/uploads/";              // 实际用@Value("${app.upload.dir}")
        final String urlPath = "/uploads/";                  // 实际用@Value("${app.upload.path}")

        try {
            // 1. 校验文件有效性
            if (file.isEmpty()) {
                throw new IllegalArgumentException("上传文件不能为空");
            }
            if (!Arrays.asList("image/jpeg", "image/png").contains(file.getContentType())) {
                throw new IllegalArgumentException("仅支持JPEG/PNG格式");
            }

            // 2. 生成唯一文件名
            String fileExt = getFileExtension(file.getOriginalFilename());
            String fileName = "avatar-" + UUID.randomUUID() + fileExt;

            // 3. 创建存储目录
            Path storageDir = Paths.get(uploadDir, "avatars");
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }

            // 4. 存储文件
            Path targetPath = storageDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 5. 生成访问URL
            String accessUrl = baseUrl + urlPath + "avatars/" + fileName;

            // 6. 更新数据库
            userMapper.updateAvatarUrl(userId, accessUrl);

            return accessUrl;
        } catch (IOException e) {
            throw new RuntimeException("文件存储失败: " + e.getMessage());
        }
    }
    // 辅助方法：获取文件扩展名
    private String getFileExtension(String filename) {
        return filename != null && filename.contains(".")
                ? filename.substring(filename.lastIndexOf("."))
                : "";
    }
}