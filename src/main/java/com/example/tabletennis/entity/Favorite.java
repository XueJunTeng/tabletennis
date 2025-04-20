package com.example.tabletennis.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 收藏实体类
 */
@Setter
@Getter
@Data
public class Favorite {
    private Long favoriteId;
    private Long userId;     // 使用 Long 类型
    private Integer contentId;
    private LocalDateTime createdTime;
}