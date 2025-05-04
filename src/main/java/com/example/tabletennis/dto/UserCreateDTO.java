package com.example.tabletennis.dto;

import com.example.tabletennis.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateDTO {
    @NotBlank
    private String username;

    @Email
    private String email;

    @Size(min = 1)
    private String password;

    private Role role;
}

