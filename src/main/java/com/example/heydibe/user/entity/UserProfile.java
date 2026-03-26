package com.example.heydibe.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "profile_image_url", nullable = true, length = 500)
    private String profileImageUrl;

    public void updateProfileImageUrl(String profileImageUrl) {
        if (profileImageUrl != null && !profileImageUrl.isBlank()) {
            this.profileImageUrl = profileImageUrl;
        }
    }
}
