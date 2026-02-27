package com.example.heydibe.user.service;

import com.example.heydibe.common.exception.BusinessException;
import com.example.heydibe.common.exception.ErrorCode;
import com.example.heydibe.user.entity.UserProfile;
import com.example.heydibe.user.repository.UserProfileRepository;
import com.example.heydibe.user.entity.User;
import com.example.heydibe.user.repository.UserRepository;
import com.example.heydibe.user.request.SignupRequest;
import com.example.heydibe.user.response.SignupResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String username = normalizeUsername(request.getUsername());
        checkUsernameDuplicate(username);

        String passwordHash = hashPassword(request.getPassword());

        User user = saveUser(username, passwordHash, request.getNickname());
        String profileKey = (request.getProfileImageKey() == null || request.getProfileImageKey().isBlank())
                ? "profiles/default.png"
                : request.getProfileImageKey();

        userProfileRepository.save(
                UserProfile.builder()
                        .userId(user.getId())
                        .profileImageKey(profileKey)
                        .build()
        );

        return SignupResponse.from(user);
    }

    @Transactional
    public User withdraw(Long userId) {
        User user = findUserByIdIncludingDeleted(userId);
        checkAlreadyDeleted(user);
        updateDeletedAt(user);
        return userRepository.save(user);
    }

    public String normalizeUsername(String raw) {
        return raw == null ? null : raw.trim().toLowerCase();
    }

    public void checkUsernameDuplicate(String username) {
        if (userRepository.existsByUsernameAndDeletedAtIsNull(username)) {
            throw new BusinessException(ErrorCode.USERNAME_DUPLICATED);
        }
    }

    public String hashPassword(String rawPassword) {
        try {
            return passwordEncoder.encode(rawPassword);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public User saveUser(String username, String passwordHash, String nickname) {
        try {
            User user = User.builder()
                    .username(username)
                    .passwordHash(passwordHash)
                    .nickname(nickname)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .deletedAt(null)
                    .build();
            return userRepository.save(user);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public User findByIdAndDeletedAtIsNull(Long userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    public User findUserByIdIncludingDeleted(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    public void checkAlreadyDeleted(User user) {
        if (user.isDeleted()) {
            throw new BusinessException(ErrorCode.ALREADY_DELETED_USER);
        }
    }

    public void updateDeletedAt(User user) {
        user.softDelete();
    }
}

