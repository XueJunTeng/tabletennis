package com.example.tabletennis.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 点赞实体类
 */
@Setter
@Getter
@Data
public class Like {
    private Integer likeId;
    private Integer contentId;
    private Long userId; // 修改为Long类型以匹配AuthService中的用户ID
    private LocalDateTime createdTime;
}
