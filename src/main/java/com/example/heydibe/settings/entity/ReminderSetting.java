package com.example.heydibe.settings.entity;

import com.example.heydibe.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

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

    @Column(name = "reminder_time")
    private LocalTime reminderTime;

    @Column(name = "days_of_week", length = 50)
    private String daysOfWeek;
}