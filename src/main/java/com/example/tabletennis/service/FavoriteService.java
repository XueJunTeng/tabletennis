package com.example.tabletennis.service;

import com.example.tabletennis.dto.FavoriteResponse;
import com.example.tabletennis.entity.UserBehavior;
import com.example.tabletennis.mapper.FavoriteMapper;
import com.example.tabletennis.mapper.UserBehaviorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteMapper favoriteMapper;
    private final UserBehaviorMapper userBehaviorMapper;
    private final ContentService contentService;

    @Transactional
    public FavoriteResponse toggleFavorite(Long userId, Integer contentId) {
        boolean isFavorited = favoriteMapper.existsFavorite(userId, contentId);

        if (isFavorited) {
            // 取消收藏
            favoriteMapper.deleteFavorite(userId, contentId);
            contentService.decrementFavoriteCount(contentId);
            userBehaviorMapper.updateBehaviorStatus(userId, contentId, "favorite", false);
        } else {
            // 新增收藏
            favoriteMapper.insertFavorite(userId, contentId);
            contentService.incrementFavoriteCount(contentId);
            UserBehavior behavior = new UserBehavior();
            behavior.setUserId(userId);
            behavior.setContentId(contentId);
            behavior.setBehaviorType("favorite");
            behavior.setIsActive(true);
            userBehaviorMapper.insertBehavior(behavior);
        }

        int newCount = contentService.getFavoriteCount(contentId);
        return new FavoriteResponse(!isFavorited, newCount);
    }
}