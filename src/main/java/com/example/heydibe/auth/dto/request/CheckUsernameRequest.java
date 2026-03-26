package com.example.heydibe.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CheckUsernameRequest {

    @NotBlank(message = "username is required")
    @Size(max = 255, message = "username length is too long")
    private String username;
}
