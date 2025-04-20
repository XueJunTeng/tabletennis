// Recommendation.java
package com.example.tabletennis.entity;

import com.example.tabletennis.enums.ContentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {
    private Long userId;
    private Integer contentId;
    private Float recommendScore;
    private String strategy;
    private LocalDateTime expireTime;
    private LocalDateTime createdTime;
    // 新增元数据字段
    private ContentMetadata contentMetadata;
    // 嵌套元数据类

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentMetadata {
        private String title;
        private String coverImageUrl;
        private String author;
        private String description;
        private Integer likeCount;
        private Integer commentCount;
        private Integer favoriteCount;
        private Integer viewCount;
        private LocalDateTime createdTime;
        private List<Tag> tags;
        private ContentType Type;
    }

    public Recommendation(Long userId, Integer contentId, Float recommendScore, String strategy) {
        this.userId = userId;
        this.contentId = contentId;
        this.recommendScore = recommendScore;
        this.strategy = strategy;
        this.createdTime = LocalDateTime.now();
    }
}
