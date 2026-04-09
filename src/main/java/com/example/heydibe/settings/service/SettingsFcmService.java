package com.example.heydibe.settings.service;

import com.example.heydibe.settings.entity.FcmToken;
import com.example.heydibe.settings.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettingsFcmService {

    private final FcmTokenRepository fcmTokenRepository;

    @Transactional
    public void registerToken(Long userId, String token) {
        FcmToken fcmToken = fcmTokenRepository.findByUserId(userId)
                .orElseGet(() -> FcmToken.builder()
                        .userId(userId)
                        .fcmToken(token)
                        .build());

        fcmToken.updateToken(token);
        fcmTokenRepository.save(fcmToken);
    }
}