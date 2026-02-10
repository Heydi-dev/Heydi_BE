package com.example.heydibe.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "social_account",
       uniqueConstraints = @UniqueConstraint(name = "idx_social_provider_user", columnNames = {"provider", "provider_user_id"}))
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "provider", nullable = false, length = 20)
    private String provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(name = "access_token", length = 2048)
    private String accessToken;

    @Column(name = "refresh_token", length = 2048)
    private String refreshToken;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public void updateTokens(String accessToken, String refreshToken, LocalDateTime tokenExpiresAt) {
        if (accessToken != null) {
            this.accessToken = accessToken;
        }
        if (refreshToken != null) {
            this.refreshToken = refreshToken;
        }
        if (tokenExpiresAt != null) {
            this.tokenExpiresAt = tokenExpiresAt;
        }
    }
}
