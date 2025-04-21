// 新增分页请求DTO
package com.example.tabletennis.dto;

import lombok.Data;

@Data
public class PageRequestDTO {
    private Integer page;     // 当前页码
    private Integer size;    // 每页数量
    private String search;        // 搜索关键词
    private String status;        // 状态过滤
    private String role;          // 角色过滤
    private String sortField = "registration_time"; // 新增排序字段
    private String sortOrder = "DESC";              // 新增排序顺序
}