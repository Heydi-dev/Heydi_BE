package com.example.heydibe.settings.repository;

import com.example.heydibe.settings.entity.ReminderSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReminderSettingRepository extends JpaRepository<ReminderSetting, Long> {
    Optional<ReminderSetting> findByUser_Id(Long userId);
}