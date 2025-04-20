package com.example.tabletennis.service;

import com.example.tabletennis.dto.LikeResponse;
import com.example.tabletennis.dto.NotificationDTO;
import com.example.tabletennis.entity.UserBehavior;
import com.example.tabletennis.enums.NotificationType;
import com.example.tabletennis.mapper.LikeMapper;
import com.example.tabletennis.mapper.UserBehaviorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeMapper likeMapper;
    private final UserBehaviorMapper userBehaviorMapper;
    private final ContentService contentService;
    private final NotificationService notificationService;

    @Transactional
    public LikeResponse toggleLike(Long userId, Integer contentId) {
        boolean isLiked = likeMapper.existsLike(userId, contentId);

        if (isLiked) {
            handleUnlike(userId, contentId);
        } else {
            handleLike(userId, contentId);
        }

        int newCount = contentService.getLikeCount(contentId);
        return new LikeResponse(!isLiked, newCount);
    }

    private void handleLike(Long userId, Integer contentId) {
        likeMapper.insertLike(userId, contentId);
        contentService.incrementLikeCount(contentId);
        // 创建通知
        notificationService.createLikeNotification(userId, contentId);
        // 记录用户行为
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setContentId(contentId);
        behavior.setBehaviorType("like");
        behavior.setIsActive(true);
        userBehaviorMapper.insertBehavior(behavior);
    }

    private void handleUnlike(Long userId, Integer contentId) {
        likeMapper.deleteLike(userId, contentId);
        contentService.decrementLikeCount(contentId);
        userBehaviorMapper.updateBehaviorStatus(userId, contentId, "like", false);
    }
}