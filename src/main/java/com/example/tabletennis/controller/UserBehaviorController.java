package com.example.tabletennis.controller;


import com.example.tabletennis.entity.Content;
import com.example.tabletennis.service.AuthService;
import com.example.tabletennis.service.ContentService;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class UserBehaviorController {

    private final ContentService contentService;
    private final AuthService authService;

    @GetMapping("/{behaviorType}")
    public ResponseEntity<PageInfo<Content>> getHistoryContents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable String behaviorType,
            @AuthenticationPrincipal UserDetails userDetails
            ) {
        Long userId = authService.getUserIdByUsername(userDetails.getUsername());

        return ResponseEntity.ok(
                new PageInfo<>(contentService.getContentsByUsertype(userId, behaviorType, page, size)));
    }
}
