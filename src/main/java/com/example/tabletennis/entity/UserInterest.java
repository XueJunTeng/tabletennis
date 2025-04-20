package com.example.tabletennis.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserInterest {
    private Long userId;
    private Integer tagId;
    private Float score;
    private LocalDateTime lastUpdated;
}