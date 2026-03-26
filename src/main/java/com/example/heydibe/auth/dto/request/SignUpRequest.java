package com.example.heydibe.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {

    @NotBlank(message = "username is required")
    @Size(max = 255, message = "username length is too long")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 255, message = "password must be between 8 and 255 characters")
    private String password;

    @NotBlank(message = "nickname is required")
    @Size(max = 50, message = "nickname length is too long")
    private String nickname;
}
