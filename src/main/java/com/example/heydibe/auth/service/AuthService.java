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
import com.example.heydibe.user.entity.SocialAccount;
import com.example.heydibe.user.entity.User;
import com.example.heydibe.user.entity.UserProfile;
import com.example.heydibe.user.repository.DeviceTokenRepository;
import com.example.heydibe.user.repository.SocialAccountRepository;
import com.example.heydibe.user.repository.UserProfileRepository;
import com.example.heydibe.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.heydibe.security.util.SessionKeys.LOGIN_USER;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    public LoginResponse login(LoginRequest request, HttpSession session) {
        // 입력값 검증
        String username = request.getUsername().trim();
        if (username.isBlank()) {
            throw new CustomException(ErrorCode.REQUIRED_FIELD_MISSING);
        }

        // username으로 User 조회 (대소문자 구별, 탈퇴한 사용자 제외)
        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

        // 비밀번호 검증
        verifyPassword(request.getPassword(), user.getPasswordHash());

        // fcm_token을 device_token 테이블에 저장/업데이트
        if (request.getFcm_token() != null && !request.getFcm_token().isBlank()) {
            upsertDeviceToken(user.getId(), request.getFcm_token());
        }

        // 세션 생성 및 HttpSession에 userId 저장
        createSession(session, user.getId());

        // device_token 테이블에서 가장 최근 fcm_token 조회
        String fcmToken = deviceTokenRepository.findLatestByUserId(user.getId())
                .map(DeviceToken::getFcmToken)
                .orElse(null);

        return LoginResponse.from(user, fcmToken);
    }

    public CheckUsernameResponse checkUsername(CheckUsernameRequest request) {
        // 입력값 검증
        String username = request.getUsername().trim();
        if (username.isBlank()) {
            throw new CustomException(ErrorCode.REQUIRED_FIELD_MISSING);
        }

        // DB 조회 (대소문자 구별, 탈퇴한 사용자는 제외하고 중복 체크)
        boolean duplicate = userRepository.existsByUsernameAndDeletedAtIsNull(username);

        return CheckUsernameResponse.from(duplicate);
    }

    public void logout(HttpSession session) {
        // 세션 저장소에서 JSESSIONID 조회
        if (session == null) {
            return;
        }

        // 해당 세션 invalidate
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
        // 입력값 체크
        String username = request.getUsername().trim();
        String password = request.getPassword();
        String nickname = request.getNickname().trim();

        if (username.isBlank() || password.isBlank() || nickname.isBlank()) {
            throw new CustomException(ErrorCode.REQUIRED_FIELD_MISSING);
        }

        // 탈퇴/중복 아이디 체크 + 추가 검증
        checkUsernameDuplicate(username);

        // 프로필 이미지 검증 및 업로드
        String profileImageUrl = s3Service.uploadProfileImage(profileImage);

        // 패스워드 암호화 (BCrypt)
        String passwordHash = passwordEncoder.encode(password);

        // User 엔티티 생성
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

        // UserProfile 엔티티 생성
        UserProfile userProfile = UserProfile.builder()
                .userId(user.getId())
                .profileImageUrl(profileImageUrl)
                .build();

        userProfileRepository.save(userProfile);

        return SignUpResponse.from(user);
    }

    @Transactional
    public WithdrawResponse withdraw(HttpSession session) {
        // 세션 존재 여부 확인
        if (session == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 세션에서 user id 추출
        Long userId = getUserIdFromSession(session);

        // 유저 존재여부 확인 (탈퇴한 사용자 제외)
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        // 연관데이터 정리
        UserProfile userProfile = userProfileRepository.findByUserId(userId).orElse(null);
        if (userProfile != null) {
            // S3에 저장된 데이터 삭제
            s3Service.deleteProfileImage(userProfile.getProfileImageUrl());
            userProfileRepository.delete(userProfile);
        }

        // device_token 테이블에서 해당 user의 모든 토큰 삭제
        deviceTokenRepository.deleteAllByUserId(userId);

        // 소셜 계정이 있으면 삭제 (소셜 로그인 사용자인 경우)
        // 자체 로그인 사용자는 소셜 계정이 없으므로 삭제 안 됨
        List<SocialAccount> socialAccounts = socialAccountRepository.findByUserId(userId);
        if (!socialAccounts.isEmpty()) {
            socialAccountRepository.deleteAllByUserId(userId);
        }

        // users 테이블 delete (하드딜리트)
        userRepository.delete(user);

        // 세션 invalidate
        try {
            session.invalidate();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.SESSION_ERROR);
        }

        return WithdrawResponse.from(true);
    }

    // ----------------- 유틸리티 메소드 ------------------

    public void checkUsernameDuplicate(String username) {
        // 탈퇴한 사용자의 username은 재사용 가능하므로, 활성 사용자만 중복 체크
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

    // device_token 테이블에 FCM token 저장/업데이트 (UPSERT)
    private void upsertDeviceToken(Long userId, String fcmToken) {
        // 기존에 같은 user_id와 fcm_token 조합이 있는지 확인
        Optional<DeviceToken> existingToken = deviceTokenRepository.findByUserIdAndFcmToken(userId, fcmToken);
        
        if (existingToken.isPresent()) {
            // 이미 존재하면 last_active_at만 업데이트
            DeviceToken deviceToken = existingToken.get();
            deviceToken.updateLastActiveAt();
            deviceTokenRepository.save(deviceToken);
        } else {
            // 없으면 새로 생성
            DeviceToken deviceToken = DeviceToken.builder()
                    .userId(userId)
                    .fcmToken(fcmToken)
                    .lastActiveAt(LocalDateTime.now())
                    .build();
            deviceTokenRepository.save(deviceToken);
        }
    }
}
