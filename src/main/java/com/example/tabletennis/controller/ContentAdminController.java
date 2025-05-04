package com.example.tabletennis.controller;

import com.example.tabletennis.dto.ReviewRequest;
import com.example.tabletennis.entity.Content;
import com.example.tabletennis.service.AuthService;
import com.example.tabletennis.service.ContentService;
import com.github.pagehelper.PageInfo;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/admin/contents")
@PreAuthorize("hasRole('ADMIN')")
public class ContentAdminController {
    private final ContentService contentService;
    private final AuthService userService;

    public ContentAdminController(ContentService contentService, AuthService userService) {
        this.contentService = contentService;
        this.userService = userService;
    }

    @GetMapping("/pending")
    public ResponseEntity<PageInfo<Content>> getPendingContents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,  // 新增搜索关键词
            @RequestParam(required = false) String type) {  // 新增内容类型过滤
        return ResponseEntity.ok(
                new PageInfo<>(contentService.getPendingContents(
                        page,
                        size,
                        keyword,
                        type)));
    }
    @GetMapping("/list")
    public ResponseEntity<PageInfo<Content>> getContents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,  // 新增搜索关键词
            @RequestParam(required = false) String type) {  // 新增内容类型过滤
        return ResponseEntity.ok(
                new PageInfo<>(contentService.getContents(
                        page,
                        size,
                        keyword,
                        type)));
    }
    @PostMapping("/{contentId}/review")
    public ResponseEntity<Void> reviewContent(
            @PathVariable Integer contentId,
            @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails)
    {
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        contentService.reviewContent(contentId,userId,
                request.getStatus(),
                request.getReviewNotes());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{contentId}")
    public ResponseEntity<Void> deleteTag(@PathVariable Integer contentId) {
        contentService.deleteContent(contentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/batch-delete")
    public ResponseEntity<Void> batchDeleteTags(@RequestBody Map<String, List<Integer>> request) {
        contentService.batchDeleteContents(request.get("contentIds"));
        return ResponseEntity.noContent().build();
    }

}