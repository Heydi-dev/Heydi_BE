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

    @Column(name = "profile_image_key", nullable = false, length = 500)
    private String profileImageKey;

    public void updateProfileImageKey(String key) {
        if (key != null && !key.isBlank()) {
            this.profileImageKey = key;
        }
    }
}
