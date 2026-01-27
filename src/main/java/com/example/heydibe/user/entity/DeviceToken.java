package com.example.heydibe.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "device_token", 
       uniqueConstraints = @UniqueConstraint(name = "idx_device_user_token", columnNames = {"user_id", "fcm_token"}))
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "fcm_token", nullable = false, length = 500)
    private String fcmToken;

    @Column(name = "last_active_at", nullable = false)
    private LocalDateTime lastActiveAt;

    public void updateLastActiveAt() {
        this.lastActiveAt = LocalDateTime.now();
    }
}
