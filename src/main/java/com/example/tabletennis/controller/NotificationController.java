// NotificationController.java
package com.example.tabletennis.controller;
import com.example.tabletennis.dto.ApiResponse;
import com.example.tabletennis.dto.BatchReadRequest;
import com.example.tabletennis.entity.Content;
import com.example.tabletennis.entity.Notification;
import com.example.tabletennis.enums.NotificationType;
import com.example.tabletennis.mapper.UserMapper;
import com.example.tabletennis.service.AuthService;
import com.example.tabletennis.service.NotificationService;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    // 获取所有通知通知
    @GetMapping
    public ResponseEntity<PageInfo<Notification>> getNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = authService.getUserIdByUsername(userDetails.getUsername());

        return ResponseEntity.ok(
                new PageInfo<>(notificationService.getNotifications(userId,page,size)));
    }
    @PostMapping("/batch-read")
    public ResponseEntity<ApiResponse> batchMarkAsRead(
            @RequestBody BatchReadRequest request
            ) {
        notificationService.batchMarkAsRead(request.getIds());
        return ResponseEntity.ok(ApiResponse.success("标记成功"));
    }

    // 新增的未读数接口
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = authService.getUserIdByUsername(userDetails.getUsername());
        int count = notificationService.countUnreadNotifications(userId);
        return ResponseEntity.ok(
                ApiResponse.success("获取成功", Map.of("count", count))
        );
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

