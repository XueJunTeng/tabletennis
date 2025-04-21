// src/main/java/com/example/tabletennis/service/ContentService.java
package com.example.tabletennis.service;

import com.example.tabletennis.dto.ContentUploadDTO;
import com.example.tabletennis.entity.Content;
import com.example.tabletennis.entity.User;
import com.github.pagehelper.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ContentService {

    // 获取已审核视频列表
    List<Content> getApprovedVideos();
    // 获取视频详情
    Content getVideoById(Integer contentId);

    // 增加点赞数
    public void incrementLikeCount(Integer contentId) ;

    // 减少点赞数
    public void decrementLikeCount(Integer contentId) ;

    // 获取当前点赞数
    public int getLikeCount(Integer contentId);

    // 增加收藏数
    void incrementFavoriteCount(Integer contentId);

    // 减少收藏数
    void decrementFavoriteCount(Integer contentId);

    // 获取当前收藏数
    int getFavoriteCount(Integer contentId);

    // 创建内容（含文件上传）
    Content createContent(ContentUploadDTO dto,
                          MultipartFile contentFile,
                          MultipartFile coverImage,
                          User user);

    //增加浏览量
    Content getVideoByIdWithViewCount(Integer contentId,Long userId);

    // 新增方法
    Map<Integer, Float> getContentTagVector(Integer contentId);

    Page<Content> getContentsByStatus(String status, int page, int size);
    void reviewContent(Integer contentId, String status, String reviewNotes);

    Long getContentUserId(Integer contentId);

    Page<Content> getContentsByUsertype(Long userId, String behaviorType, int page, int size);

    List<Content> getApprovedArticles();

    List<Content> getSearchContents(String query);

    List<Content> getPendingContents(int page, int size, String keyword, String type);
}