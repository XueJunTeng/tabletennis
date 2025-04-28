package com.example.tabletennis.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论实体类
 */
@Setter
@Getter
// Comment.java
@Data
public class Comment {
    private Long commentId;
    private String content;
    private Long contentId;
    private Long userId;
    private Long parentId;
    private LocalDateTime createTime;
    private String status = "APPROVED";
    private String username;
    private String avatarUrl;   // 新增字段
}