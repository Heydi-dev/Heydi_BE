package com.example.heydibe.settings.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fcm_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "fcm_token", nullable = false, length = 512)
    private String fcmToken;

    public void updateToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}