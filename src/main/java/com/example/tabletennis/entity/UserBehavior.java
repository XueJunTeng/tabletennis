package com.example.tabletennis.entity;

import com.example.tabletennis.enums.BehaviorType;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户行为日志实体类
 */
@Setter
@Getter
@Data
public class UserBehavior {
    private Long logId;
    private Long userId;
    private Integer contentId;
    private String behaviorType;
    private Boolean isActive;
    private Integer weight;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}