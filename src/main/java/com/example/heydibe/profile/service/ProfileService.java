package com.example.heydibe.profile.service;

import com.example.heydibe.common.exception.BusinessException;
import com.example.heydibe.common.exception.ErrorCode;
import com.example.heydibe.user.entity.UserProfile;
import com.example.heydibe.user.repository.UserProfileRepository;
import com.example.heydibe.profile.request.ProfileUpdateRequest;
import com.example.heydibe.profile.response.UserProfileResponse;
import com.example.heydibe.user.entity.User;
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

    public UserProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));

        return UserProfileResponse.from(user.getUsername(), user.getNickname(), profile.getProfileImageKey());
    }

    @Transactional
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));

        // nickname 변경
        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            user.updateNickname(request.getNickname());
        }

        // 비밀번호 변경 (옵션)
        validatePasswordConfirm(request.getNewPassword(), request.getNewPasswordConfirm());
        String hashed = hashPasswordIfPresent(request.getNewPassword());
        if (hashed != null) {
            user.updatePasswordHash(hashed);
        }

        // 프로필 이미지 키 변경 (옵션)
        profile.updateProfileImageKey(request.getProfileImageKey());

        userRepository.save(user);
        userProfileRepository.save(profile);
    }

    public void validatePasswordConfirm(String newPassword, String confirm) {
        if (newPassword == null && confirm == null) return;
        if (newPassword == null || confirm == null || !newPassword.equals(confirm)) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
    }

    public String hashPasswordIfPresent(String newPassword) {
        if (newPassword == null || newPassword.isBlank()) return null;
        try {
            return passwordEncoder.encode(newPassword);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
