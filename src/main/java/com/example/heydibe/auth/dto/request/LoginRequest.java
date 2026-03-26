package com.example.heydibe.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "username is required")
    @Size(max = 255, message = "username length is too long")
    private String username;

    @NotBlank(message = "password is required")
    @Size(max = 255, message = "password length is too long")
    private String password;

    @NotBlank(message = "fcm_token is required")
    private String fcm_token;
}
