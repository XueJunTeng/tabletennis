package com.example.tabletennis.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 公告实体类
 */
@Setter
@Getter
public class Announcement {
    // Getters & Setters
    private Integer announcementId; // 公告ID
    private String title;           // 标题
    private String content;         // 内容
    private LocalDateTime publishTime; // 发布时间
    private Integer publisherId;    // 发布者ID（管理员）

}
