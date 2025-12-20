package com.example.heydibe.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class LoginRequest {

    @NotBlank(message = "username?� ?�수?�니??")
    @Size(max = 255, message = "username 길이가 ?�무 깁니??")
    private String username;

    @NotBlank(message = "password???�수?�니??")
    @Size(max = 255, message = "password 길이가 ?�무 깁니??")
    private String password;
}

