package com.example.heydibe.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class LoginRequest {

    @NotBlank(message = "username is required")
    @Size(max = 255, message = "username length is too long")
    private String username;

    @NotBlank(message = "password is required")
    @Size(max = 255, message = "password length is too long")
    private String password;
}
