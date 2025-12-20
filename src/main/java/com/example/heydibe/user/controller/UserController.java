package com.example.heydibe.user.controller;

import com.example.heydibe.auth.AuthDto;
import com.example.heydibe.auth.service.AuthService;
import com.example.heydibe.common.ApiResponse;
import com.example.heydibe.user.request.SignupRequest;
import com.example.heydibe.user.response.SignupResponse;
import com.example.heydibe.user.response.WithdrawResponse;
import com.example.heydibe.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.success(userService.signup(request));
    }

    @PostMapping("/withdraw")
    public ApiResponse<WithdrawResponse> withdraw(HttpSession session) {
        AuthDto auth = authService.getLoginUserFromSession(session);
        userService.withdraw(auth.getUserId());

        // ?ˆí‡´ ??ì¦‰ì‹œ ë¡œê·¸?„ì›ƒ(?¸ì…˜ ë¬´íš¨??
        try {
            session.invalidate();
        } catch (Exception e) {
            // invalidate ?¤íŒ¨??500?¼ë¡œ ì²˜ë¦¬?˜ë„ë¡??ˆì™¸ë¡??˜ì ¸????
            // ?¬ê¸°?œëŠ” ê°„ë‹¨??BusinessException?¼ë¡œ ?˜ê¸°ê³??¶ìœ¼ë©?AuthService.logout(session) ?¸ì¶œë¡??€ì²?ê°€??
            throw e;
        }

        return ApiResponse.success(WithdrawResponse.from(true));
    }
}

