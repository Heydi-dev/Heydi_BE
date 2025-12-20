package com.example.heydibe.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CheckUsernameRequest {

    @NotBlank(message = "username은 필수입니다")
    @Size(max = 255, message = "username 길이가 너무 깁니다")
    private String username;
}
