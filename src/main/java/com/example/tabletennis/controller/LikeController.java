package com.example.tabletennis.controller;

import com.example.tabletennis.dto.LikeResponse;
import com.example.tabletennis.service.AuthService;
import com.example.tabletennis.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;
    private final AuthService authService;

    @PostMapping("/{contentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LikeResponse> addLike(
            @PathVariable Integer contentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = authService.getUserIdByUsername(userDetails.getUsername());
        LikeResponse response = likeService.toggleLike(userId, contentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{contentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LikeResponse> removeLike(
            @PathVariable Integer contentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = authService.getUserIdByUsername(userDetails.getUsername());
        LikeResponse response = likeService.toggleLike(userId, contentId);
        return ResponseEntity.ok(response);
    }
}