// RecommendationController.java
package com.example.tabletennis.controller;

import com.example.tabletennis.entity.Content;
import com.example.tabletennis.entity.Recommendation;
import com.example.tabletennis.entity.User;
import com.example.tabletennis.mapper.ContentMapper;
import com.example.tabletennis.mapper.RecommendationMapper;
import com.example.tabletennis.service.AuthService;
import com.example.tabletennis.service.ContentService;
import com.example.tabletennis.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;     // 解决 Function 符号问题
import java.util.stream.Collectors;     // 确保 Stream 操作正常
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationMapper recommendationMapper;
    private final ContentMapper contentMapper;
    private final ContentService contentService;
    private final RecommendationService recommendationService;
    private final AuthService authService;

    //计算所有用户的喜好，录入推荐缓存表
    @GetMapping
    public List<Content> getRecommendations(@AuthenticationPrincipal User user) {
        List<Recommendation> recs = recommendationMapper.selectByUser(user.getUserId());
        return recs.stream()
                .map(rec -> contentService.getVideoById(rec.getContentId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    //实时推荐
    @GetMapping("/real-time")
    public List<Recommendation> getRealTimeRecommendations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "20") int limit) {
        Long userId = authService.getUserIdByUsername(userDetails.getUsername());

        // 1. 生成两种推荐结果
        List<Recommendation> contentBased = recommendationService.generateContentBasedRecommendations(userId, 50);
        List<Recommendation> collabBased = recommendationService.generateCollaborativeRecommendations(userId, 50);

        // 2. 合并推荐得分
        Map<Integer, Float> combinedScores = new HashMap<>();
        contentBased.forEach(r -> combinedScores.merge(r.getContentId(), r.getRecommendScore() * 0.6f, Float::sum));
        collabBased.forEach(r -> combinedScores.merge(r.getContentId(), r.getRecommendScore() * 0.4f, Float::sum));

        // 3. 提取需要元数据的contentIds
        List<Integer> contentIds = new ArrayList<>(combinedScores.keySet());

        // 4. 批量获取内容元数据（包含热度指标）
        Map<Integer, Content> contentMap = contentMapper.selectBatchIds(contentIds).stream()
                .collect(Collectors.toMap(Content::getContentId, Function.identity()));

        // 5. 计算热度分并合并到总评分
        combinedScores.forEach((contentId, score) -> {
            Content content = contentMap.get(contentId);
            if (content != null) {
                // 热度计算公式（可调整权重）
                float popularityScore = calculatePopularityScore(content);
                combinedScores.put(contentId, score * 0.7f + popularityScore * 0.3f);
            }
        });

        // 6. 构建最终推荐结果
        return combinedScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Float>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> createRecommendation(userId, entry, contentMap.get(entry.getKey())))
                .collect(Collectors.toList());
    }

    // 热度计算函数（使用对数平滑处理）
    private float calculatePopularityScore(Content content) {
        // 基础指标（防止零值）
        float likes = Math.max(content.getLikeCount(), 1);
        float favorites = Math.max(content.getFavoriteCount(), 1);
        float comments = Math.max(content.getCommentCount(), 1);
        float views = Math.max(content.getViewCount(), 1);

        // 使用对数处理避免极端值（系数可调整）
        return (float) (
                Math.log(likes) * 0.4 +
                        Math.log(favorites) * 0.3 +
                        Math.log(comments) * 0.2 +
                        Math.log(views) * 0.1
        );
    }

    // 创建推荐对象（重构后的方法）
    private Recommendation createRecommendation(Long userId, Map.Entry<Integer, Float> entry, Content content) {
        Recommendation.ContentMetadata metadata = new Recommendation.ContentMetadata();
        metadata.setTitle(content.getTitle());
        metadata.setCoverImageUrl(content.getCoverImageUrl());
        metadata.setAuthor(content.getAuthor());
        metadata.setDescription(content.getDescription());
        metadata.setLikeCount(content.getLikeCount());
        metadata.setCommentCount(content.getCommentCount());
        metadata.setFavoriteCount(content.getFavoriteCount());
        metadata.setViewCount(content.getViewCount());
        metadata.setCreatedTime(content.getCreatedTime());
        metadata.setTags(content.getTags());
        metadata.setType(content.getType());

        return new Recommendation(
                userId,
                entry.getKey(),
                entry.getValue(), // 最终得分已包含热度
                "real-time",
                LocalDateTime.now().plusHours(24),
                LocalDateTime.now(),
                metadata
        );
    }

}