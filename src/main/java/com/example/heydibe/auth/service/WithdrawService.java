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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Slf4j
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
        String profileImageUrl = userProfile != null ? userProfile.getProfileImageUrl() : null;

        List<SocialAccount> socialAccounts = socialAccountRepository.findByUserId(userId);
        List<SocialUnlinkService.SocialUnlinkTarget> unlinkTargets =
                socialUnlinkService.prepareUnlinkTargets(socialAccounts);

        if (userProfile != null) {
            userProfileRepository.delete(userProfile);
        }

        deviceTokenRepository.deleteAllByUserId(userId);

        if (!socialAccounts.isEmpty()) {
            socialAccountRepository.deleteAllByUserId(userId);
        }

        user.anonymizeForWithdrawal();
        userRepository.save(user);

        registerCleanupAfterCommit(new WithdrawCleanup(profileImageUrl, unlinkTargets));
    }

    private void registerCleanupAfterCommit(WithdrawCleanup cleanup) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            runCleanup(cleanup);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                runCleanup(cleanup);
            }
        });
    }

    private void runCleanup(WithdrawCleanup cleanup) {
        if (cleanup.profileImageUrl() != null && !cleanup.profileImageUrl().isBlank()) {
            try {
                s3Service.deleteProfileImage(cleanup.profileImageUrl());
            } catch (Exception e) {
                log.warn("Failed to delete profile image during withdraw cleanup", e);
            }
        }

        socialUnlinkService.unlinkBestEffort(cleanup.unlinkTargets());
    }

    private record WithdrawCleanup(
            String profileImageUrl,
            List<SocialUnlinkService.SocialUnlinkTarget> unlinkTargets
    ) {}
}
