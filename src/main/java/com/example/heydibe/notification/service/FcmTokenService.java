package com.example.heydibe.notification.service;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.notification.dto.request.FcmTokenRequest;
import com.example.heydibe.notification.dto.request.FcmTestRequest;
import com.example.heydibe.user.entity.User;
import com.example.heydibe.user.repository.UserRepository;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FcmTokenService {

    private final UserRepository userRepository;
    private final FcmService fcmService;

    public void registerToken(Long userId, FcmTokenRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        user.updateFcmToken(request.getFcmToken());
    }

    public String sendTestNotification(Long userId, FcmTestRequest request) throws FirebaseMessagingException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (user.getFcmToken() == null || user.getFcmToken().isBlank()) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        return fcmService.sendMessage(
                user.getFcmToken(),
                request.getTitle(),
                request.getBody()
        );
    }
}