package com.example.tabletennis.controller;

import com.example.tabletennis.dto.ContentUploadDTO;
import com.example.tabletennis.entity.Content;
import com.example.tabletennis.entity.User;
import com.example.tabletennis.entity.UserBehavior;
import com.example.tabletennis.enums.ContentStatus;
import com.example.tabletennis.enums.ContentType;
import com.example.tabletennis.service.AuthService;
import com.example.tabletennis.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {
    private final ContentService contentService;
    private final AuthService authService;

    //获取搜索内容列表
    @GetMapping("/search/{query}")
    public ResponseEntity<List<Content>> getSearchContents(@PathVariable String query) {
        List<Content> contents = contentService.getSearchContents(query);
        return ResponseEntity.ok(contents);
    }


    // 获取已审核视频列表
    @GetMapping("/videos")
    public ResponseEntity<List<Content>> getApprovedVideos() {
        List<Content> videos = contentService.getApprovedVideos();
        return ResponseEntity.ok(videos);
    }
    // 获取已审核文章列表
    @GetMapping("/articles")
    public ResponseEntity<List<Content>> getApprovedArticles() {
        List<Content> videos = contentService.getApprovedArticles();
        return ResponseEntity.ok(videos);
    }

    // 获取视频详情
    @GetMapping("/videos/{contentId}")
    public ResponseEntity<Content> getVideoDetail(
            @PathVariable Integer contentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            Long userId = authService.getUserIdByUsername(userDetails.getUsername());
            Content video = contentService.getVideoByIdWithViewCount(contentId, userId);
            return ResponseEntity.ok(video);
        } catch (Exception e) {
            // 统一异常处理
            if (e instanceof ResponseStatusException) {
                throw e;
            }
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "视频不存在或已被删除",
                    e
            );
        }
    }
    // 获取文章详情
    @GetMapping("/articles/{contentId}")
    public ResponseEntity<Content> getArticleDetail(
            @PathVariable Integer contentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            Long userId = authService.getUserIdByUsername(userDetails.getUsername());
            Content video = contentService.getVideoByIdWithViewCount(contentId, userId);
            return ResponseEntity.ok(video);
        } catch (Exception e) {
            // 统一异常处理
            if (e instanceof ResponseStatusException) {
                throw e;
            }
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "视频不存在或已被删除",
                    e
            );
        }
    }
    // 新增上传内容接口
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Content> uploadContent(
            @Valid @RequestPart("dto") ContentUploadDTO dto,
            @RequestPart(value = "contentFile", required = false) MultipartFile contentFile, // 改为非必须
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @AuthenticationPrincipal User user) {
        try {
            // 动态文件校验
            if (dto.getType() == ContentType.VIDEO) {
                if (contentFile == null || contentFile.isEmpty()) {
                    throw new IllegalArgumentException("视频类内容必须上传文件");
                }
                if (!isValidVideoType(contentFile)) {
                    throw new IllegalArgumentException("仅支持MP4/AVI视频格式");
                }
            }

            Content content = contentService.createContent(dto, contentFile, coverImage, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(content);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "内容上传失败: " + e.getMessage(),
                    e
            );
        }
    }

    // 视频类型校验工具方法
    private boolean isValidVideoType(MultipartFile file) {
        String[] allowed = {"video/mp4", "video/avi"};
        return Arrays.asList(allowed).contains(file.getContentType());
    }


}