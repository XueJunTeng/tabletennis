package com.example.tabletennis.service;

import com.example.tabletennis.dto.BatchOperationDTO;
import com.example.tabletennis.dto.PageRequestDTO;
import com.example.tabletennis.dto.PageResponseDTO;
import com.example.tabletennis.entity.User;
import com.example.tabletennis.exception.ResourceNotFoundException;
import com.example.tabletennis.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    public PageResponseDTO<User> getUsersByPage(PageRequestDTO request) {
        Map<String, Object> params = buildConditionParams(request);

        int total = userMapper.countUsersByCondition(params);
        List<User> users = userMapper.selectUsersByCondition(params);

        return PageResponseDTO.<User>builder()
                .total(total)
                .currentPage(request.getPage())
                .pageSize(request.getSize())
                .data(users)
                .build();
    }

    private Map<String, Object> buildConditionParams(PageRequestDTO request) {
        Map<String, Object> params = new HashMap<>();
        // 分页参数计算
        int offset = (request.getPage() - 1) * request.getSize();
        params.put("offset", offset);
        params.put("size", request.getSize());

        // 查询条件
        if (request.getSearch() != null && !request.getSearch().isEmpty()) {
            params.put("search", "%" + request.getSearch().trim() + "%");
        }
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            params.put("status", request.getStatus());
        }
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            params.put("role", request.getRole());
        }
        return params;


    }
    // 修改用户状态
    public void updateUserStatus(Long userId, String status) {
        if (!Arrays.asList("active", "disabled").contains(status)) {
            throw new IllegalArgumentException("Invalid status");
        }
        int result = userMapper.updateUserStatus(userId, status);
        if (result == 0) {
            throw new ResourceNotFoundException("User not found");
        }
    }

    // 修改用户角色
    public void updateUserRole(Long userId, String role) {
        if (!Arrays.asList("ADMIN", "USER", "EDITOR").contains(role)) {
            throw new IllegalArgumentException("Invalid role");
        }
        int result = userMapper.updateUserRole(userId, role);
        if (result == 0) {
            throw new ResourceNotFoundException("User not found");
        }
    }

    // 批量操作
    @Transactional
    public void batchOperation(BatchOperationDTO dto) {
        switch (dto.getOperation().toLowerCase()) {
            case "delete":
                userMapper.batchDeleteUsers(dto.getUserIds());
                break;
            case "enable":
                userMapper.batchUpdateStatus(dto.getUserIds(), "active");
                break;
            case "disable":
                userMapper.batchUpdateStatus(dto.getUserIds(), "disabled");
                break;
            case "change_role":
                if (dto.getNewRole() == null) {
                    throw new IllegalArgumentException("New role required");
                }
                userMapper.batchUpdateRoles(dto.getUserIds(), dto.getNewRole());
                break;
            default:
                throw new IllegalArgumentException("Invalid operation");
        }
    }

    // 导出用户数据
    public List<User> getAllUsersForExport(PageRequestDTO request) {
        Map<String, Object> params = buildConditionParams(request);
        params.remove("offset");  // 移除分页限制
        params.remove("size");
        return userMapper.selectUsersByCondition(params);
    }


}