package com.example.tabletennis.service;

// 导入必要的类和包
import com.example.tabletennis.entity.*;
import com.example.tabletennis.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


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

        // 4. 生成推荐结果并排序（添加日志输出）
        List<Recommendation> recommendations = contentScores.entrySet().stream()
                // 添加中间操作打印日志
                .peek(entry -> System.out.printf(
                        "[内容推荐] 内容ID：%-5d | 相似度得分：%.3f | 用户ID：%d%n",
                        entry.getKey(), entry.getValue(), userId
                ))
                .sorted(Map.Entry.<Integer, Float>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new Recommendation(
                        userId,
                        entry.getKey(),
                        entry.getValue(),
                        "content"
                ))
                .collect(Collectors.toList());

        // 添加最终结果汇总输出
        System.out.println("\n=== 基于内容的推荐结果汇总 ===");
        System.out.printf("用户ID：%d | 生成推荐数量：%d/%d%n",
                userId, recommendations.size(), limit);
        System.out.println("--------------------------------");
        recommendations.forEach(rec -> System.out.printf(
                "内容ID：%-5d | 最终得分：%.3f | 策略：%-7s%n",
                rec.getContentId(), rec.getRecommendScore(), rec.getStrategy()
        ));
        System.out.println("================================");

        return recommendations;
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
        Set<Integer> targetContentIds = targetBehaviors.stream()
                .map(UserBehavior::getContentId)
                .collect(Collectors.toSet());

        // 2. 计算相似用户（Jaccard相似度）
        Map<Long, Float> similarUsers = new HashMap<>();
        List<UserBehavior> allBehaviors = userBehaviorMapper.selectAll();

        System.out.println("\n=== Jaccard相似度计算明细 ===");
        System.out.println("目标用户ID: " + userId);
        System.out.println("目标用户行为内容: " + targetContentIds);
        System.out.println("-----------------------------------------------");
        System.out.println("其他用户ID | 交集大小 | 并集大小 | 相似度 | 用户行为内容");

        allBehaviors.stream()
                .filter(b -> !b.getUserId().equals(userId))
                .collect(Collectors.groupingBy(UserBehavior::getUserId))
                .forEach((otherUserId, behaviors) -> {
                    // 获取其他用户的行为内容集合
                    Set<Integer> otherItems = behaviors.stream()
                            .map(UserBehavior::getContentId)
                            .collect(Collectors.toSet());

                    // 计算交集和并集
                    Set<Integer> intersection = new HashSet<>(targetContentIds);
                    intersection.retainAll(otherItems);
                    int unionSize = targetContentIds.size() + otherItems.size() - intersection.size();

                    // 计算相似度（处理除零情况）
                    float similarity = unionSize == 0 ? 0 :
                            (float) intersection.size() / unionSize;

                    // 记录调试信息
                    System.out.printf("%-10d | %-8d | %-8d | %-6.2f | %s%n",
                            otherUserId,
                            intersection.size(),
                            unionSize,
                            similarity,
                            otherItems);

                    similarUsers.put(otherUserId, similarity);
                });

        // 添加空行分隔
        System.out.println("\n=== 相似度计算结果 ===");
        similarUsers.forEach((otherUserId, similarity) ->
                System.out.printf("用户 %d 的相似度: %.2f%n", otherUserId, similarity));


        // 3. 获取Top10相似用户（过滤零相似）
        List<Long> topSimilarUsers = similarUsers.entrySet().stream()
                .filter(entry -> entry.getValue() > 0) // 过滤相似度<=0的用户
                .sorted(Map.Entry.<Long, Float>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .toList();

        // 4. 收集相似用户行为（使用相似度加权）
        Map<Integer, Float> weightedScores = new HashMap<>();
        topSimilarUsers.forEach(similarUserId -> {
            // 获取该相似用户的相似度值
            float similarity = similarUsers.get(similarUserId);

            // 获取用户交互内容的唯一集合
            Set<Integer> userContents = userBehaviorMapper.selectByUser(similarUserId)
                    .stream()
                    .map(UserBehavior::getContentId)
                    .collect(Collectors.toSet());

            // 遍历唯一内容ID，用相似度加权
            userContents.forEach(contentId -> {
                if (!targetContentIds.contains(contentId)) {
                    weightedScores.merge(
                            contentId,
                            similarity,  // 使用相似度值作为权重
                            Float::sum
                    );
                }
            });

            // 调试日志：显示每个相似用户的贡献
            System.out.printf("[加权处理] 相似用户%-5d | 相似度:%.2f | 推荐内容: %s%n",
                    similarUserId, similarity, userContents);
        });

        // 5. 得分标准化处理（基于最大相似度）
        Map<Integer, Float> normalizedScores = normalizeWeightedScores(weightedScores);

        // 6. 生成推荐结果（带诊断日志）
        List<Recommendation> recommendations = normalizedScores.entrySet().stream()
                .peek(entry -> {
                    Float rawScore = weightedScores.get(entry.getKey());
                    float normalized = entry.getValue();
                    System.out.printf("[协同过滤诊断] 内容%-6d | 原始次数:%-3f | 归一化:%.2f | 用户%-4d%n",
                            entry.getKey(), rawScore, normalized, userId);
                })
                .sorted(Map.Entry.<Integer, Float>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new Recommendation(
                        userId,
                        entry.getKey(),
                        entry.getValue(), // 使用归一化后的得分
                        "collab"
                ))
                .collect(Collectors.toList());

        // 7. 结果汇总输出
        System.out.println("\n=== 协同过滤推荐结果 ===");
        System.out.printf("用户ID:%-5d | 有效推荐:%-2d/%-2d | 相似用户数:%-2d%n",
                userId, recommendations.size(), limit, topSimilarUsers.size());
        System.out.println("内容ID  归一化得分  原始次数");
        recommendations.forEach(rec ->
                System.out.printf("%-7d %-10.2f %-5f%n",
                        rec.getContentId(),
                        rec.getRecommendScore(),
                        weightedScores.get(rec.getContentId()))
        );
        System.out.println("=======================");

        return recommendations;
    }


    /**
     * 归一化加权得分（将得分缩放到0-1范围）
     */
    private Map<Integer, Float> normalizeWeightedScores(Map<Integer, Float> weightedScores) {
        if (weightedScores.isEmpty()) return Collections.emptyMap();

        // 获取最大得分（防止除零）
        float max = Collections.max(weightedScores.values());
        if (max == 0) return weightedScores; // 所有得分都是0

        return weightedScores.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() / max
                ));
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