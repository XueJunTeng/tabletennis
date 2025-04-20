package com.example.tabletennis.controller;

import com.example.tabletennis.dto.FavoriteResponse;
import com.example.tabletennis.service.AuthService;
import com.example.tabletennis.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final AuthService authService;

    @PostMapping("/{contentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FavoriteResponse> addFavorite(
            @PathVariable Integer contentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = authService.getUserIdByUsername(userDetails.getUsername());
        FavoriteResponse response = favoriteService.toggleFavorite(userId, contentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{contentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FavoriteResponse> removeFavorite(
            @PathVariable Integer contentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = authService.getUserIdByUsername(userDetails.getUsername());
        FavoriteResponse response = favoriteService.toggleFavorite(userId, contentId);
        return ResponseEntity.ok(response);
    }
}