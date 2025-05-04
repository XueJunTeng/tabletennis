package com.example.tabletennis.controller;

import com.alibaba.excel.EasyExcel;
import com.example.tabletennis.dto.BatchOperationDTO;
import com.example.tabletennis.dto.PageRequestDTO;
import com.example.tabletennis.dto.PageResponseDTO;
import com.example.tabletennis.dto.UserCreateDTO;
import com.example.tabletennis.entity.User;
import com.example.tabletennis.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // 仅管理员可访问
public class UserController {
    private final UserService userService;

    @GetMapping
    public PageResponseDTO<User> getUsers(PageRequestDTO request) {
        return userService.getUsersByPage(request);
    }
    // 修改用户状态
    @PatchMapping("/{userId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long userId,
                                          @RequestParam String status) {
        userService.updateUserStatus(userId, status);
        return ResponseEntity.ok().build();
    }

    // 修改用户角色
    @PatchMapping("/{userId}/role")
    public ResponseEntity<?> updateRole(@PathVariable Long userId,
                                        @RequestParam String role) {
        userService.updateUserRole(userId, role);
        return ResponseEntity.ok().build();
    }

    // 创建用户
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserCreateDTO createDTO) {
        return ResponseEntity.ok(userService.createUser(createDTO));
    }
    // 批量操作
    @PostMapping("/batch-operations")
    public ResponseEntity<?> batchOperation(@RequestBody BatchOperationDTO dto) {
        userService.batchOperation(dto);
        return ResponseEntity.ok().build();
    }

    // 导出用户数据
    @GetMapping("/export")
    public void exportUsers(PageRequestDTO request, HttpServletResponse response) {
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=users.xlsx");

        List<User> users = userService.getAllUsersForExport(request);
        // 使用EasyExcel导出（需添加依赖）
        try {
            EasyExcel.write(response.getOutputStream(), User.class)
                    .sheet("Users")
                    .doWrite(users);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}