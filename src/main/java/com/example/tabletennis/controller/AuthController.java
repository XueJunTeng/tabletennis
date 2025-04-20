// AuthController.java
package com.example.tabletennis.controller;

import com.example.tabletennis.dto.*;
import com.example.tabletennis.entity.User;
import com.example.tabletennis.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 用户注册
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        User user = authService.register(request);
        return buildAuthResponse(user);
    }

    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        User user = authService.login(request);
        return buildAuthResponse(user);
    }

    // 更新用户资料
    @PostMapping("/profile")
    public ResponseEntity<AuthResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ProfileUpdateRequest request) {
        Long userId = authService.getUserIdByUsername(userDetails.getUsername());
        User updatedUser = authService.updateUserProfile(userId, request);
        return buildAuthResponse(updatedUser);
    }

    // 修改密码
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PasswordChangeRequest request) {
        Long userId = authService.getUserIdByUsername(userDetails.getUsername());
        authService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success("密码修改成功"));
    }

    // 头像上传
    @PostMapping("/upload-avatar")
    public ResponseEntity<AvatarResponse> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("avatar") MultipartFile file) {  // 修改参数名为avatar
        Long userId = authService.getUserIdByUsername(userDetails.getUsername());
        String avatarUrl = authService.uploadAvatar(userId, file);
        return ResponseEntity.ok(new AvatarResponse(avatarUrl));
    }

    // 公共响应构建方法
    private ResponseEntity<AuthResponse> buildAuthResponse(User user) {
        String token = authService.generateToken(user);
        return ResponseEntity.ok(
                new AuthResponse(
                        token,
                        user.getUserId(),
                        user.getUsername(),
                        user.getRole().name(),
                        user.getAvatarUrl(),
                        user.getEmail()
                )
        );
    }
}

