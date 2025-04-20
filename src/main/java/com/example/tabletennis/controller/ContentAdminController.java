package com.example.tabletennis.controller;

import com.example.tabletennis.dto.ReviewRequest;
import com.example.tabletennis.entity.Content;
import com.example.tabletennis.service.ContentService;
import com.github.pagehelper.PageInfo;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin/contents")
@PreAuthorize("hasRole('ADMIN')")
public class ContentAdminController {
    private final ContentService contentService;

    public ContentAdminController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/pending")
    public ResponseEntity<PageInfo<Content>> getPendingContents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                new PageInfo<>(contentService.getContentsByStatus("pending", page, size)));
    }

    @PostMapping("/{contentId}/review")
    public ResponseEntity<Void> reviewContent(
            @PathVariable Integer contentId,
            @RequestBody ReviewRequest request) {

        contentService.reviewContent(contentId,
                request.getStatus(),
                request.getReviewNotes());
        return ResponseEntity.ok().build();
    }

}