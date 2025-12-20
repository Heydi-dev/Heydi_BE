package com.example.heydibe.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignupRequest {

    @NotBlank(message = "username?€ ?„ìˆ˜?…ë‹ˆ??")
    @Size(max = 255, message = "username ê¸¸ì´ê°€ ?ˆë¬´ ê¹ë‹ˆ??")
    private String username;

    @NotBlank(message = "password???„ìˆ˜?…ë‹ˆ??")
    @Size(min = 8, max = 255, message = "password??8~255?ì—¬???©ë‹ˆ??")
    private String password;

    @NotBlank(message = "nickname?€ ?„ìˆ˜?…ë‹ˆ??")
    @Size(max = 50, message = "nickname ê¸¸ì´ê°€ ?ˆë¬´ ê¹ë‹ˆ??")
    private String nickname;

    // presignedë¡??¬ë¦° S3 objectKey
    @Size(max = 500, message = "profileImageKey ê¸¸ì´ê°€ ?ˆë¬´ ê¹ë‹ˆ??")
    private String profileImageKey;
}

