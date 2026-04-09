package com.example.heydibe.mypage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MyPageResponse {

    private Long userId;
    private String nickname;
    private String profileImageUrl;

    private long likedPostCount;
    private long sharedPostCount;

    private Alarm alarm;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Alarm {
        private boolean enabled;
    }
}