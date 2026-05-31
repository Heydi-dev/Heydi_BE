package com.example.heydibe.user.dto;

import com.example.heydibe.user.entity.User;

public record UserPublicProfile(
        Long userId,
        String nickname,
        String profileImageUrl,
        boolean withdrawn
) {
    public static UserPublicProfile withdrawn(Long userId) {
        return new UserPublicProfile(userId, User.WITHDRAWN_NICKNAME, null, true);
    }
}
