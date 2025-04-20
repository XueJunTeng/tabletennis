package com.example.tabletennis.service;

// 导入必要的类和包
import com.example.tabletennis.entity.*;
import com.example.tabletennis.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
        * 推荐系统服务类，负责处理内容推荐相关业务逻辑
 * 包含基于内容的推荐、协同过滤推荐和混合推荐策略
 */
@Service
@RequiredArgsConstructor // 自动生成构造函数注入依赖
public class RecommendationService {
    // 数据访问层接口依赖注入
    private final UserInterestMapper userInterestMapper;    // 用户兴趣数据访问
    private final ContentMapper contentMapper;              // 内容数据访问
    private final ContentTagMapper contentTagMapper;        // 内容标签关系数据访问
    private final TagMapper tagMapper;                      // 标签数据访问
    private final UserBehaviorMapper userBehaviorMapper;   // 用户行为数据访问
    private final RecommendationMapper recommendationMapper; // 推荐结果数据访问
    private final UserMapper userMapper;                    // 用户数据访问
    private final ContentService contentService;            // 内容业务服务

    /**
    * 生成基于内容的推荐（内容相似性推荐）
    * @param userId 目标用户ID
    * @param limit 推荐结果数量限制
    * @return 推荐内容列表，按相似度降序排列
    */
    public List<Recommendation> generateContentBasedRecommendations(Long userId, int limit) {
        // 1. 获取用户兴趣向量（标签权重字典）
        Map<Integer, Float> userInterest = getUserInterestVector(userId);

        // 2. 获取所有已审核通过的内容
        List<Content> allContents = contentMapper.selectApprovedContents();

        // 3. 计算内容相似度得分
        Map<Integer, Float> contentScores = new HashMap<>();
        for (Content content : allContents) {
            // 获取内容标签向量
            Map<Integer, Float> contentTags = contentService.getContentTagVector(content.getContentId());
            // 计算余弦相似度
            float similarity = calculateCosineSimilarity(userInterest, contentTags);
            contentScores.put(content.getContentId(), similarity);
        }

        // 4. 生成推荐结果并排序
        return contentScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Float>comparingByValue().reversed()) // 按得分降序排序
                .limit(limit) // 限制结果数量
                .map(entry -> new Recommendation(
                        userId,
                        entry.getKey(),
                        entry.getValue(),
                        "content" // 推荐策略标记
                ))
                .collect(Collectors.toList());
    }

    /**
    * 生成协同过滤推荐（用户行为相似性推荐）
    * @param userId 目标用户ID
    * @param limit 推荐结果数量限制
    * @return 推荐内容列表，按推荐得分降序排列
    */
    public List<Recommendation> generateCollaborativeRecommendations(Long userId, int limit) {
        // 1. 获取目标用户行为记录
        List<UserBehavior> targetBehaviors = userBehaviorMapper.selectByUser(userId);

        // 2. 计算相似用户（基于共同行为物品的Jaccard相似度）
        Map<Long, Float> similarUsers = new HashMap<>();
        List<UserBehavior> allBehaviors = userBehaviorMapper.selectAll();

        for (UserBehavior behavior : allBehaviors) {
            if (behavior.getUserId().equals(userId)) continue; // 跳过目标用户自身

            // 获取两个用户的行为内容集合
            Set<Integer> targetItems = targetBehaviors.stream()
                    .map(UserBehavior::getContentId)
                    .collect(Collectors.toSet());
            Set<Integer> otherItems = allBehaviors.stream()
                    .filter(b -> b.getUserId().equals(behavior.getUserId()))
                    .map(UserBehavior::getContentId)
                    .collect(Collectors.toSet());

            // 计算Jaccard相似度：交集大小 / 并集大小
            Set<Integer> intersection = new HashSet<>(targetItems);
            intersection.retainAll(otherItems);
            float similarity = (float) intersection.size() /
                    (targetItems.size() + otherItems.size() - intersection.size());

            similarUsers.merge(behavior.getUserId(), similarity, Float::sum);
        }

        // 3. 获取相似度最高的前10个用户
        List<Long> topSimilarUsers = similarUsers.entrySet().stream()
                .sorted(Map.Entry.<Long, Float>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 4. 收集相似用户的行为内容（排除目标用户已接触的内容）
        Set<Integer> targetContentIds = targetBehaviors.stream()
                .map(UserBehavior::getContentId)
                .collect(Collectors.toSet());
        Map<Integer, Integer> contentScores = new HashMap<>();

        for (Long similarUserId : topSimilarUsers) {
            userBehaviorMapper.selectByUser(similarUserId).forEach(behavior -> {
                if (!targetContentIds.contains(behavior.getContentId())) {
                    // 合并内容推荐得分（出现次数累计）
                    contentScores.merge(behavior.getContentId(), 1, Integer::sum);
                }
            });
        }

        // 5. 生成推荐结果并排序
        return contentScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new Recommendation(
                        userId,
                        entry.getKey(),
                        entry.getValue().floatValue(),
                        "collab" // 推荐策略标记
                ))
                .collect(Collectors.toList());
    }

    /**
    * 获取用户兴趣向量（标签权重字典）
    * @param userId 用户ID
    * @return 标签ID到兴趣得分的映射表
    */
    private Map<Integer, Float> getUserInterestVector(Long userId) {
        return userInterestMapper.selectByUserId(userId).stream()
                .collect(Collectors.toMap(
                        UserInterest::getTagId,
                        UserInterest::getScore
                ));
    }

    /**
    * 计算余弦相似度
    * @param vec1 向量1（用户兴趣向量）
    * @param vec2 向量2（内容标签向量）
    * @return 相似度得分（0-1之间）
    */
    private float calculateCosineSimilarity(Map<Integer, Float> vec1, Map<Integer, Float> vec2) {
        // 获取共同标签集合
        Set<Integer> commonTags = new HashSet<>(vec1.keySet());
        commonTags.retainAll(vec2.keySet());

        // 计算点积
        float dotProduct = 0;
        for (int tagId : commonTags) {
            dotProduct += vec1.get(tagId) * vec2.get(tagId);
        }

        // 计算向量模长
        float norm1 = 0, norm2 = 0;
        for (float v : vec1.values()) norm1 += v * v;
        for (float v : vec2.values()) norm2 += v * v;

        // 避免除以零的情况
        if (norm1 == 0 || norm2 == 0) return 0;

        // 返回余弦相似度
        return (float) (dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2)));
    }

    /**
     * 混合推荐策略（定时任务）
     * 每天执行一次，组合内容推荐和协同过滤推荐结果
     * 推荐结果存储到数据库，有效期24小时
     */
    @Scheduled(fixedRate = 86400000) // 24小时执行一次
    public void updateRecommendations() {
        // 遍历所有活跃用户
        userMapper.selectAllActiveUsers().forEach(user -> {
            // 生成两种推荐结果各50条
            List<Recommendation> contentBased = generateContentBasedRecommendations(user.getUserId(), 50);
            List<Recommendation> collabBased = generateCollaborativeRecommendations(user.getUserId(), 50);

            // 合并推荐得分（内容推荐权重60%，协同过滤40%）
            Map<Integer, Float> combinedScores = new HashMap<>();
            contentBased.forEach(r -> combinedScores.merge(
                    r.getContentId(),
                    r.getRecommendScore() * 0.6f,
                    Float::sum
            ));
            collabBased.forEach(r -> combinedScores.merge(
                    r.getContentId(),
                    r.getRecommendScore() * 0.4f,
                    Float::sum
            ));

            // 更新数据库推荐结果
            recommendationMapper.deleteByUser(user.getUserId()); // 清除旧推荐
            combinedScores.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Float>comparingByValue().reversed())
                    .limit(20) // 最终保留前20条推荐
                    .forEach(entry -> {
                        Recommendation rec = new Recommendation();
                        rec.setUserId(user.getUserId());
                        rec.setContentId(entry.getKey());
                        rec.setRecommendScore(entry.getValue());
                        rec.setStrategy("hybrid"); // 混合策略标记
                        rec.setExpireTime(LocalDateTime.now().plusDays(1)); // 24小时后过期
                        recommendationMapper.insert(rec); // 插入新推荐
                    });
        });
    }

}