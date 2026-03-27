package com.example.heydibe.auth.service;

import com.example.heydibe.auth.dto.request.CheckUsernameRequest;
import com.example.heydibe.auth.dto.request.LoginRequest;
import com.example.heydibe.auth.dto.request.SignUpRequest;
import com.example.heydibe.auth.dto.response.CheckUsernameResponse;
import com.example.heydibe.auth.dto.response.LoginResponse;
import com.example.heydibe.auth.dto.response.SignUpResponse;
import com.example.heydibe.auth.dto.response.WithdrawResponse;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.infrastructure.s3.S3Service;
import com.example.heydibe.user.entity.DeviceToken;
import com.example.heydibe.user.entity.User;
import com.example.heydibe.user.entity.UserProfile;
import com.example.heydibe.user.repository.DeviceTokenRepository;
import com.example.heydibe.user.repository.UserProfileRepository;
import com.example.heydibe.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.heydibe.security.util.SessionKeys.LOGIN_USER;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final WithdrawService withdrawService;

    public LoginResponse login(LoginRequest request, HttpSession session) {
        String username = request.getUsername().trim();
        if (username.isBlank()) {
            throw new CustomException(ErrorCode.REQUIRED_FIELD_MISSING);
        }

        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

        verifyPassword(request.getPassword(), user.getPasswordHash());

        // fcmToken을 device_token 테이블에 저장/업데이트
        if (request.getFcmToken() != null && !request.getFcmToken().isBlank()) {
            upsertDeviceToken(user.getId(), request.getFcmToken());
        }

        createSession(session, user.getId());

        String fcmToken = deviceTokenRepository.findLatestByUserId(user.getId())
                .map(DeviceToken::getFcmToken)
                .orElse(null);

        return LoginResponse.from(user, fcmToken);
    }

    public CheckUsernameResponse checkUsername(CheckUsernameRequest request) {
        String username = request.getUsername().trim();
        if (username.isBlank()) {
            throw new CustomException(ErrorCode.REQUIRED_FIELD_MISSING);
        }

        boolean duplicate = userRepository.existsByUsernameAndDeletedAtIsNull(username);

        return CheckUsernameResponse.from(duplicate);
    }

    public void logout(HttpSession session) {
        if (session == null) {
            return;
        }

        try {
            session.invalidate();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.SESSION_ERROR);
        }
    }

    public Long getUserIdFromSession(HttpSession session) {
        Object obj = session.getAttribute(LOGIN_USER);
        if (obj == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        return (Long) obj;
    }

    public User getLoginUserFromSession(HttpSession session) {
        Long userId = getUserIdFromSession(session);
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
    }

    @Transactional
    public SignUpResponse signup(SignUpRequest request, MultipartFile profileImage) {
        String username = request.getUsername().trim();
        String password = request.getPassword();
        String nickname = request.getNickname().trim();

        if (username.isBlank() || password.isBlank() || nickname.isBlank()) {
            throw new CustomException(ErrorCode.REQUIRED_FIELD_MISSING);
        }

        checkUsernameDuplicate(username);

        String profileImageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            profileImageUrl = s3Service.uploadProfileImage(profileImage);
        }

        String passwordHash = passwordEncoder.encode(password);

        User user = User.builder()
                .username(username)
                .passwordHash(passwordHash)
                .nickname(nickname)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(null)
                .fcmToken(null)
                .build();

        userRepository.save(user);

        UserProfile userProfile = UserProfile.builder()
                .userId(user.getId())
                .profileImageUrl(profileImageUrl)
                .build();

        userProfileRepository.save(userProfile);

        return SignUpResponse.from(user);
    }

    @Transactional
    public WithdrawResponse withdraw(HttpSession session) {
        if (session == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = getUserIdFromSession(session);
        withdrawService.withdraw(userId);

        try {
            session.invalidate();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.SESSION_ERROR);
        }

        return WithdrawResponse.from(true);
    }

    public void checkUsernameDuplicate(String username) {
        if (userRepository.existsByUsernameAndDeletedAtIsNull(username)) {
            throw new CustomException(ErrorCode.USERNAME_DUPLICATED);
        }
    }

    public void verifyPassword(String rawPassword, String passwordHash) {
        if (passwordHash == null || !passwordEncoder.matches(rawPassword, passwordHash)) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }
    }

    public void createSession(HttpSession session, Long userId) {
        try {
            session.setAttribute(LOGIN_USER, userId);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.SESSION_ERROR);
        }
    }

    private void upsertDeviceToken(Long userId, String fcmToken) {
        Optional<DeviceToken> existingToken = deviceTokenRepository.findByUserIdAndFcmToken(userId, fcmToken);

        if (existingToken.isPresent()) {
            DeviceToken deviceToken = existingToken.get();
            deviceToken.updateLastActiveAt();
            deviceTokenRepository.save(deviceToken);
        } else {
            DeviceToken deviceToken = DeviceToken.builder()
                    .userId(userId)
                    .fcmToken(fcmToken)
                    .lastActiveAt(LocalDateTime.now())
                    .build();
            deviceTokenRepository.save(deviceToken);
        }
    }
}