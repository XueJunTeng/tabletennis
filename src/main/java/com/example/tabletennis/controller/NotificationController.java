// NotificationController.java
package com.example.tabletennis.controller;
import com.example.tabletennis.dto.ApiResponse;
import com.example.tabletennis.entity.Content;
import com.example.tabletennis.entity.Notification;
import com.example.tabletennis.enums.NotificationType;
import com.example.tabletennis.mapper.UserMapper;
import com.example.tabletennis.service.AuthService;
import com.example.tabletennis.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    // 获取所有通知通知
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = authService.getUserIdByUsername(userDetails.getUsername());
        List<Notification> Notifications= notificationService.getNotifications(userId);
        return ResponseEntity.ok(Notifications);
    }



    // 统一异常处理（示例）
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleBadRequest(Exception e) {
        return ResponseEntity.badRequest().body(
                ApiResponse.error(400, e.getMessage())
        );
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse> handleUnauthorized(Exception e) {
        return ResponseEntity.status(401).body(
                ApiResponse.error(401, "请先登录")
        );
    }
}

