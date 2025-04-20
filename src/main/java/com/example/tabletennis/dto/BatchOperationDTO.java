package com.example.tabletennis.dto;

import lombok.Data;

import java.util.List;

// BatchOperationDTO.java
@Data
public class BatchOperationDTO {
    private List<Long> userIds;
    private String operation; // enable/disable/delete
    private String newRole;   // 仅用于角色修改
}
