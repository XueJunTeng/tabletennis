package com.example.tabletennis.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.example.tabletennis.enums.ContentStatus;
import com.example.tabletennis.enums.ContentType;
import lombok.*;

/**
 * 内容实体类（与数据库 content 表映射）
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Content {
    // 核心字段
    private Integer contentId;
    private String title;
    private String description;
    private Long userId;
    private LocalDateTime createdTime;
    private LocalDateTime lastModifiedTime;  // 新增字段
    private ContentType type;
    private String filePath;
    private String coverImageUrl;

    // 统计字段
    private Integer viewCount;
    private Integer likeCount;      // 新增字段
    private Integer commentCount;   // 新增字段
    private Integer favoriteCount;  // 新增字段

    // 状态与关联
    private ContentStatus status;
    private List<Tag> tags;

    // 非持久化字段（用于业务展示）
    private String author;
    private String authorUrl;
    private String reviewNotes; // 审核备注
    // 优化后的 toString（隐藏敏感字段）
    @Override
    public String toString() {
        return "Content{" +
                "contentId=" + contentId +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", viewCount=" + viewCount +
                ", likeCount=" + likeCount +
                ", status=" + status +
                ", tags=" + tags +  // 添加 tags 字段
                '}';
    }
}