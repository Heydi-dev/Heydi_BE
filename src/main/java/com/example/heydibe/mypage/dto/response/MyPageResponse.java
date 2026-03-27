package com.example.heydibe.mypage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyPageResponse {

    private Long userId;
    private String username;
    private String nickname;
    private String profileImageUrl;
    private boolean hasPassword;

    public static MyPageResponse from(
            Long userId,
            String username,
            String nickname,
            String profileImageUrl,
            boolean hasPassword
    ) {
        return new MyPageResponse(
                userId,
                username,
                nickname,
                profileImageUrl,
                hasPassword
        );
    }
}