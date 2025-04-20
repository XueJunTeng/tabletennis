// AuthResponse.java
package com.example.tabletennis.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuthResponse {
    private final String token;
    private final Long userId;
    private final String username;
    private final String role;
    private final String avatarUrl;
    private final String email;
}