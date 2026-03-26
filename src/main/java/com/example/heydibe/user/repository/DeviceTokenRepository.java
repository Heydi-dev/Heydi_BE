package com.example.heydibe.user.repository;

import com.example.heydibe.user.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByUserIdAndFcmToken(Long userId, String fcmToken);

    List<DeviceToken> findByUserId(Long userId);

    @Query("SELECT dt FROM DeviceToken dt WHERE dt.userId = :userId ORDER BY dt.lastActiveAt DESC")
    Optional<DeviceToken> findLatestByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM DeviceToken dt WHERE dt.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
