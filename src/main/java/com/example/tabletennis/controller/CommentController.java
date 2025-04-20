package com.example.tabletennis.controller;

import com.example.tabletennis.dto.CommentDTO;
import com.example.tabletennis.dto.CommentRequest;
import com.example.tabletennis.service.AuthService;
import com.example.tabletennis.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contents/{contentId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final AuthService userService;

    @GetMapping
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Long contentId) {
        return ResponseEntity.ok(commentService.getCommentsByContentId(contentId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable Long contentId,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        CommentDTO newComment = commentService.addComment(contentId, userId, request.getContent(), request.getParentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
    }
}