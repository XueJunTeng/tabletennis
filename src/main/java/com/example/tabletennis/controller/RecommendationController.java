// RecommendationController.java
package com.example.tabletennis.controller;
import com.example.tabletennis.entity.Tag;
import com.example.tabletennis.entity.Content;
import com.example.tabletennis.entity.Recommendation;
import com.example.tabletennis.entity.User;
import com.example.tabletennis.entity.UserBehavior;
import com.example.tabletennis.mapper.ContentMapper;
import com.example.tabletennis.mapper.RecommendationMapper;
import com.example.tabletennis.mapper.UserBehaviorMapper;
import com.example.tabletennis.service.AuthService;
import com.example.tabletennis.service.ContentService;
import com.example.tabletennis.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    private final UserBehaviorMapper userBehaviorMapper;
    private final ContentService contentService;
    private final RecommendationService recommendationService;
    private final AuthService authService;

    //实时推荐
    // 实时推荐接口添加过滤开关参数
    @GetMapping("/real-time")
    public List<Recommendation> getRealTimeRecommendations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "500") int limit,
            @RequestParam(defaultValue = "true") boolean filterViewed) { // 新增过滤开关

        Long userId = authService.getUserIdByUsername(userDetails.getUsername());

        // 0. 获取用户近期观看记录
        Set<Integer> viewedIds = getRecentViewedContents(userId);

        // 1. 生成两种推荐结果
        List<Recommendation> contentBased = recommendationService.generateContentBasedRecommendations(userId, 50);
        List<Recommendation> collabBased = recommendationService.generateCollaborativeRecommendations(userId, 50);

        // 2. 合并推荐得分
        Map<Integer, Float> combinedScores = new HashMap<>();
        contentBased.forEach(r -> combinedScores.merge(r.getContentId(), r.getRecommendScore() * 0.6f, Float::sum));
        collabBased.forEach(r -> combinedScores.merge(r.getContentId(), r.getRecommendScore() * 0.4f, Float::sum));

        // 3. 根据开关过滤已观看内容（调用独立方法）
        if (filterViewed) {
            filterViewedContents(combinedScores, viewedIds);
        }

        // 4. 提取需要元数据的contentIds
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
    // 独立过滤方法（新增）
    private void filterViewedContents(
            Map<Integer, Float> candidateScores,
            Set<Integer> viewedIds) {
        candidateScores.keySet().removeAll(viewedIds);
    }
    @GetMapping("/related/{contentId}")
    public List<Content> getRelatedVideos(
            @PathVariable Integer contentId,
            @RequestParam(defaultValue = "10") int count) {

        // 1. 获取当前视频元数据
        Content currentVideo = contentService.getVideoById(contentId);

        // 2. 提取当前视频标签ID
        Set<Integer> currentTags = currentVideo.getTags().stream()
                .map(Tag::getTagId)
                .collect(Collectors.toSet());

        // 3. 获取候选视频（扩大候选池）
        List<Content> candidates = contentMapper.selectVideosByTags(
                currentTags,
                contentId,
                Math.max(count * 3, 30) // 扩大候选集保证排序效果
        );

        // 4. 计算余弦相似度得分
        Map<Content, Float> scores = new LinkedHashMap<>();
        for (Content candidate : candidates) {
            // 候选视频标签集合
            Set<Integer> candidateTags = candidate.getTags().stream()
                    .map(Tag::getTagId)
                    .collect(Collectors.toSet());

            // 计算交集
            int common = (int) candidateTags.stream()
                    .filter(currentTags::contains)
                    .count();

            // 余弦相似度计算
            float cosineSim = (float) (common /
                    (Math.sqrt(currentTags.size()) * Math.sqrt(candidateTags.size())));

            // 综合得分
            float finalScore = cosineSim * 0.5f +
                    calculatePopularityScore(candidate) * 0.3f +
                    calculateFreshnessScore(candidate) * 0.2f;

            scores.put(candidate, finalScore);
        }

        // 5. 排序返回
        return scores.entrySet().stream()
                .sorted(Map.Entry.<Content, Float>comparingByValue().reversed())
                .limit(count)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // 新鲜度计算（新方法）
    private float calculateFreshnessScore(Content content) {
        // 发布时间越近分数越高（按天衰减）
        long daysOld = ChronoUnit.DAYS.between(
                content.getCreatedTime().toLocalDate(),
                LocalDate.now()
        );
        return (float) Math.exp(-daysOld * 0.1);
    }

    // 热度计算函数（使用对数平滑处理）
    private float calculatePopularityScore(Content content) {
        // 原始值（无需强制最小值）
        int rawLikes = content.getLikeCount();
        int rawFavorites = content.getFavoriteCount();
        int rawComments = content.getCommentCount();
        int rawViews = content.getViewCount();

        // 添加平滑项（避免原始值为0或1时的问题）
        float likes = rawLikes + 1;
        float favorites = rawFavorites + 1;
        float comments = rawComments + 1;
        float views = rawViews + 1;

        // 对数转换
        float logLikes = (float) Math.log(likes);
        float logFavorites = (float) Math.log(favorites);
        float logComments = (float) Math.log(comments);
        float logViews = (float) Math.log(views);

        // 加权计算
        return logLikes * 0.3f + logFavorites * 0.4f + logComments * 0.2f + logViews * 0.2f;
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
    // 新增方法：获取近期观看记录
    private Set<Integer> getRecentViewedContents(Long userId) {
        // 查询最近30天的观看记录
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        return userBehaviorMapper.selectContentsByBehavior(
                        userId,
                        "view",
                        cutoff
                ).stream()
                .map(UserBehavior::getContentId)
                .collect(Collectors.toSet());
    }
}