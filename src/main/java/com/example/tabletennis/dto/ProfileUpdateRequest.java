package com.example.tabletennis.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// ProfileUpdateRequest.java
@Getter
@Setter
public class ProfileUpdateRequest {
    @Size(min = 2, max = 20, message = "用户名长度需在2-20个字符之间")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatarUrl;
}