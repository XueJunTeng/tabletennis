package com.example.tabletennis.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserProfileResponse {
    private final Long userId;
    private final String username;
    private final String email;
    private final String avatarUrl;
}
