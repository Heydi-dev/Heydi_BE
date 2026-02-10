package com.example.heydibe.auth.service;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.infrastructure.s3.S3Service;
import com.example.heydibe.user.entity.SocialAccount;
import com.example.heydibe.user.entity.User;
import com.example.heydibe.user.entity.UserProfile;
import com.example.heydibe.user.repository.DeviceTokenRepository;
import com.example.heydibe.user.repository.SocialAccountRepository;
import com.example.heydibe.user.repository.UserProfileRepository;
import com.example.heydibe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WithdrawService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final S3Service s3Service;
    private final SocialUnlinkService socialUnlinkService;

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        UserProfile userProfile = userProfileRepository.findByUserId(userId).orElse(null);
        if (userProfile != null) {
            s3Service.deleteProfileImage(userProfile.getProfileImageUrl());
            userProfileRepository.delete(userProfile);
        }

        deviceTokenRepository.deleteAllByUserId(userId);

        List<SocialAccount> socialAccounts = socialAccountRepository.findByUserId(userId);
        if (!socialAccounts.isEmpty()) {
            socialUnlinkService.unlinkAllByUserId(userId);
            socialAccountRepository.deleteAllByUserId(userId);
        }

        userRepository.delete(user);
    }
}
