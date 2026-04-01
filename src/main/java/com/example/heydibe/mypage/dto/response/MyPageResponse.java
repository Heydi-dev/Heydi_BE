package com.example.heydibe.mypage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MyPageResponse {

    private Long userId;
    private String username;
    private String nickname;
    private String profileImageUrl;
}