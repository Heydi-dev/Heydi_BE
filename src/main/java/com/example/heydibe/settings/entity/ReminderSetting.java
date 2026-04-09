package com.example.heydibe.settings.entity;

import com.example.heydibe.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reminder_setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReminderSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "meridiem", length = 2)
    private String meridiem;

    @Column(name = "hour")
    private Integer hour;

    @Column(name = "minute")
    private Integer minute;
}