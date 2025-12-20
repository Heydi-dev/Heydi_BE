package com.example.heydibe.auth.service;

import com.example.heydibe.auth.AuthDto;
import com.example.heydibe.auth.request.CheckUsernameRequest;
import com.example.heydibe.auth.request.LoginRequest;
import com.example.heydibe.auth.response.CheckUsernameResponse;
import com.example.heydibe.auth.response.LoginResponse;
import com.example.heydibe.common.exception.BusinessException;
import com.example.heydibe.common.exception.ErrorCode;
import com.example.heydibe.user.entity.User;
import com.example.heydibe.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.example.heydibe.auth.session.SessionKeys.LOGIN_USER;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request, HttpSession session) {
        String username = normalizeUsername(request.getUsername());
        User user = getUserIncludingDeleted(username);
        verifyPassword(request.getPassword(), user.getPasswordHash());
        AuthDto auth = authenticate(user);
        createSession(session, auth);
        return LoginResponse.from(user);
    }

    public CheckUsernameResponse checkUsername(CheckUsernameRequest request) {
        String username = normalizeUsername(request.getUsername());
        boolean duplicate = isUsernameDuplicate(username);
        return CheckUsernameResponse.from(duplicate);
    }

    public void logout(HttpSession session) {
        try {
            session.invalidate();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SESSION_ERROR);
        }
    }

    public AuthDto getLoginUserFromSession(HttpSession session) {
        Object obj = session.getAttribute(LOGIN_USER);
        if (obj == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
        return (AuthDto) obj;
    }

    // ----------------- 내부 메소드 -----------------

    public String normalizeUsername(String raw) {
        return raw == null ? null : raw.trim().toLowerCase();
    }

    public User getUserIncludingDeleted(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        if (user.isDeleted()) {
            throw new BusinessException(ErrorCode.USER_DELETED);
        }
        return user;
    }

    public void verifyPassword(String rawPassword, String passwordHash) {
        if (passwordHash == null || !passwordEncoder.matches(rawPassword, passwordHash)) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
    }

    public AuthDto authenticate(User user) {
        return new AuthDto(user.getId(), user.getUsername(), user.getNickname());
    }

    public void createSession(HttpSession session, AuthDto authDto) {
        try {
            session.setAttribute(LOGIN_USER, authDto);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SESSION_ERROR);
        }
    }

    public boolean isUsernameDuplicate(String username) {
        return userRepository.existsByUsernameAndDeletedAtIsNull(username);
    }
}
