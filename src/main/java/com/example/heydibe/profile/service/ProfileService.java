package com.example.heydibe.profile.service;

import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.infrastructure.s3.S3Service;
import com.example.heydibe.profile.dto.request.ProfileUpdateRequest;
import com.example.heydibe.profile.dto.response.ProfileResponse;
import com.example.heydibe.profile.dto.response.ProfileUpdateResponse;
import com.example.heydibe.user.entity.User;
import com.example.heydibe.user.entity.UserProfile;
import com.example.heydibe.user.repository.UserProfileRepository;
import com.example.heydibe.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    public ProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        return ProfileResponse.from(
                user.getId(),
                user.getUsername(),
                profile.getProfileImageUrl(),
                user.getNickname(),
                user.getPasswordHash() != null ? "***" : null
        );
    }

    @Transactional
    public ProfileUpdateResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 이미지 처리
        String profileImageUrl = processProfileImage(request, profile);

        // 닉네임 변경
        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            user.updateNickname(request.getNickname());
        }

        // 비밀번호 변경
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            // 현재 비밀번호 확인
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
            }

            // 새 비밀번호 암호화
            String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
            user.updatePasswordHash(newPasswordHash);
        }

        userRepository.save(user);
        if (profileImageUrl != null) {
            profile.updateProfileImageUrl(profileImageUrl);
            userProfileRepository.save(profile);
        }

        return ProfileUpdateResponse.from(
                user.getId(),
                user.getUsername(),
                profile.getProfileImageUrl(),
                user.getNickname()
        );
    }

    private String processProfileImage(ProfileUpdateRequest request, UserProfile profile) {
        // deleteProfileImage가 "true"일 경우 프로필 이미지를 null로 설정 (프론트엔드에서 기본 이미지 처리)
        if ("true".equalsIgnoreCase(request.getDeleteProfileImage())) {
            // 기존 프로필 이미지 삭제
            s3Service.deleteProfileImage(profile.getProfileImageUrl());
            return null;
        }

        // profileImage 전송시 기존 프로필 삭제 후 새 프로필 이미지 업로드
        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            // 기존 프로필 이미지 삭제
            s3Service.deleteProfileImage(profile.getProfileImageUrl());
            // 새 프로필 이미지 업로드
            return s3Service.uploadProfileImage(request.getProfileImage());
        }

        // profileImage가 미전송될 경우 기존 프로필 이미지 유지
        return null;
    }
}
