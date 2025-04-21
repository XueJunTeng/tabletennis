package com.example.tabletennis.service.impl;

import com.alibaba.excel.util.StringUtils;
import com.example.tabletennis.dto.ContentUploadDTO;
import com.example.tabletennis.entity.Content;
import com.example.tabletennis.entity.ContentTag;
import com.example.tabletennis.entity.User;
import com.example.tabletennis.entity.UserBehavior;
import com.example.tabletennis.enums.ContentStatus;
import com.example.tabletennis.enums.ContentType;
import com.example.tabletennis.exception.FileStorageException;
import com.example.tabletennis.mapper.ContentMapper;
import com.example.tabletennis.mapper.ContentTagMapper;
import com.example.tabletennis.mapper.TagMapper;
import com.example.tabletennis.mapper.UserBehaviorMapper;
import com.example.tabletennis.service.ContentService;
import com.example.tabletennis.service.FileStorageService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    // 配置常量
    private static final long MAX_FILE_SIZE = 500 * 1024 * 1024; // 500MB
    private static final long MAX_COVER_SIZE = 5 * 1024 * 1024; // 5MB

    // 依赖注入
    private final ContentMapper contentMapper;
    private final TagMapper tagMapper;
    private final ContentTagMapper contentTagMapper;
    private final FileStorageService fileStorageService;
    private final UserBehaviorMapper userBehaviorMapper;

    @Override
    public List<Content> getSearchContents(String query){
        return contentMapper.selectByTitle(query);
    }
    @Override
    public List<Content> getPendingContents(int page, int size, String keyword, String type) {
        PageHelper.startPage(page, size);
        return contentMapper.selectPendingContents(
                processSearchKeyword(keyword),
                validateContentType(type)
        );
    }

    private String processSearchKeyword(String keyword) {
        return StringUtils.isBlank(keyword) ? null : keyword.trim();
    }

    private String validateContentType(String type) {
        if (StringUtils.isBlank(type)) return null;
        return Arrays.asList("VIDEO", "ARTICLE")
                .contains(type.toUpperCase()) ? type.toUpperCase() : null;
    }
    public List<Content> getApprovedVideos() {
        return contentMapper.selectApprovedVideos();
    }
    public List<Content> getApprovedArticles() {
        return contentMapper.selectApprovedArticles();
    }
    @Override
    public Content getVideoByIdWithViewCount(Integer contentId,Long userId) {
        // 1. 先递增浏览量
        contentMapper.incrementViewCount(contentId);
        //再录入行为信息
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setContentId(contentId);
        behavior.setBehaviorType("view");
        behavior.setIsActive(true);
        userBehaviorMapper.insertBehavior(behavior);
        // 2. 再获取最新数据
        return getVideoById(contentId);
    }

    @Override
    public Content getVideoById(Integer contentId) {
        Content content = contentMapper.selectContentById(contentId);
        if (content == null) {
            throw new FileStorageException("找不到ID为 " + contentId + " 的内容");
        }
        return content;
    }

    // 增加点赞数
    @Transactional
    public void incrementLikeCount(Integer contentId) {
        contentMapper.incrementLikeCount(contentId);
    }

    // 减少点赞数
    @Transactional
    public void decrementLikeCount(Integer contentId) {
        contentMapper.decrementLikeCount(contentId);
    }

    // 获取当前点赞数
    public int getLikeCount(Integer contentId) {
        return contentMapper.getLikeCountByContentId(contentId);
    }

    // 增加收藏数
    public void incrementFavoriteCount(Integer contentId){
        contentMapper.incrementFavoriteCount(contentId);
    };

    // 减少收藏数
    public void decrementFavoriteCount(Integer contentId){
        contentMapper.decrementFavoriteCount(contentId);
    };

    // 获取当前收藏数
    public int getFavoriteCount(Integer contentId){
        return contentMapper.getFavoriteCount(contentId);
    };
    @Transactional
    public void incrementViewCount(Integer contentId) {
        contentMapper.incrementViewCount(contentId);
    }


    //创建内容
    @Override
    @Transactional
    public Content createContent(ContentUploadDTO dto,
                                 MultipartFile contentFile,
                                 MultipartFile coverImage,
                                 User user) {
        // 1. 验证文件
        if(dto.getType()== ContentType.VIDEO) {
            validateContentFile(contentFile);
        }
        validateCoverImage(coverImage);

        // 2. 存储文件
        String filePath = storeContentFile(contentFile);
        String coverPath = storeCoverImageSafely(coverImage);

        // 3. 构建内容实体
        Content content = buildContentEntity(dto, user, filePath, coverPath);

        // 4. 持久化数据
        persistContent(content, dto.getTagIds());

        log.info("内容创建成功，ID：{}", content.getContentId());
        return content;
    }

    // 验证主文件有效性
    private void validateContentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的主文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("主文件大小超过限制（最大%.1fMB）", MAX_FILE_SIZE / (1024.0 * 1024))
            );
        }
    }

    // 验证封面图片有效性
    private void validateCoverImage(MultipartFile image) {
        if (image != null && !image.isEmpty() && image.getSize() > MAX_COVER_SIZE) {
            throw new IllegalArgumentException(
                    String.format("封面图片大小超过限制（最大%.1fMB）", MAX_COVER_SIZE / (1024.0 * 1024))
            );
        }
    }

    // 存储主文件（允许失败）
    private String storeContentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            return fileStorageService.storeFileWithUrl(file);
        } catch (Exception e) {
            log.error("主文件[{}]存储失败：{}", file.getOriginalFilename(), e.getMessage());
            throw new RuntimeException("系统繁忙，请稍后重试");
        }
    }

    // 安全存储封面图片（允许失败）
    private String storeCoverImageSafely(MultipartFile image) {
        if (image == null || image.isEmpty()) return null;

        try {
            return fileStorageService.storeFileWithUrl(image);
        } catch (Exception e) {
            log.warn("封面图片[{}]存储失败：{}", image.getOriginalFilename(), e.getMessage());
            return null;
        }
    }

    // 构建内容实体
    private Content buildContentEntity(ContentUploadDTO dto,
                                       User user,
                                       String filePath,
                                       String coverPath) {
        return Content.builder()
                .title(cleanString(dto.getTitle()))
                .description(cleanString(dto.getDescription()))
                .type(dto.getType())
                .filePath(filePath)
                .coverImageUrl(coverPath)
                .userId(user.getUserId())
                .status(ContentStatus.PENDING)
                .createdTime(LocalDateTime.now())
                .viewCount(0)
                .build();
    }

    // 数据清理
    private String cleanString(String input) {
        return input != null ? input.trim() : null;
    }

    // 持久化内容及标签
    @Transactional
    protected void persistContent(Content content, List<Integer> tagIds) {
        // 插入主内容
        int affectedRows = contentMapper.insertContent(content);
        if (affectedRows == 0 || content.getContentId() == null) {
            log.error("内容插入失败，标题：{}", content.getTitle());
            throw new RuntimeException("内容创建失败");
        }

        // 处理标签关联
        if (tagIds != null && !tagIds.isEmpty()) {
            validateAndInsertTags(content.getContentId(), tagIds);
        }
    }

    // 验证并插入标签
    private void validateAndInsertTags(Integer contentId, List<Integer> tagIds) {
        List<Integer> existingIds = tagMapper.filterExistingTagIds(tagIds);
        if (existingIds.size() != tagIds.size()) {
            tagIds.removeAll(existingIds);
            log.warn("检测到无效标签ID：{}", tagIds);
            throw new FileStorageException("包含无效标签ID: " + tagIds);
        }
        contentTagMapper.batchInsert(contentId, tagIds);
    }
    @Override
    public Map<Integer, Float> getContentTagVector(Integer contentId) {
        List<ContentTag> contentTags = contentTagMapper.selectByContentId(contentId);
        return contentTags.stream()
                .collect(Collectors.toMap(
                        ContentTag::getTagId,
                        ct -> tagMapper.selectById(ct.getTagId()).getWeight()
                ));
    }

    public Page<Content> getContentsByStatus(String status, int page, int size) {
        PageHelper.startPage(page, size);
        return contentMapper.selectByStatusWithPage(status);
    }
    public Page<Content> getContentsByUsertype(Long userId, String behaviorType, int page, int size){
        PageHelper.startPage(page, size);
        return contentMapper.selectByUsertypeWithPage(userId,behaviorType);
    }

    // 修改后的审核方法
    @Transactional
    public void reviewContent(Integer contentId, String status, String reviewNotes) {
        // 参数校验
        if (ContentStatus.REJECTED.name().equalsIgnoreCase(status)
                && StringUtils.isBlank(reviewNotes)) {
            throw new IllegalArgumentException("Rejection requires review notes");
        }

        // 更新数据库
        contentMapper.updateStatusWithNote(contentId, status, reviewNotes);

        // 记录审计日志
        Content content = contentMapper.selectById(contentId);
        log.info("内容审核完成 - ID: {}, 状态: {}, 操作人: 系统审核",
                contentId, status);

        // 预留通知接口
        sendNotification(content, status, reviewNotes);
    }

    private void sendNotification(Content content, String status, String notes) {
        // 空实现，仅记录日志
        log.debug("通知功能待实现 - 内容ID: {}", content.getContentId());
    }
    public Long getContentUserId(Integer contentId){
        return contentMapper.selectUserIdByContentId(contentId);
    }

}