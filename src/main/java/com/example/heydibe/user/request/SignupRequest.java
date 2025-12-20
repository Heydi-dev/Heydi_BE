package com.example.heydibe.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignupRequest {

    @NotBlank(message = "username은 필수입니다.")
    @Size(max = 255, message = "username 길이가 너무 깁니다.")
    private String username;

    @NotBlank(message = "password는 필수입니다.")
    @Size(min = 8, max = 255, message = "password는 8~255자여야 합니다.")
    private String password;

    @NotBlank(message = "nickname은 필수입니다.")
    @Size(max = 50, message = "nickname 길이가 너무 깁니다.")
    private String nickname;

    // presigned로 올린 S3 objectKey
    @Size(max = 500, message = "profileImageKey 길이가 너무 깁니다.")
    private String profileImageKey;
}
