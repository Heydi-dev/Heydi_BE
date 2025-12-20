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

        // 탈퇴 후 즉시 로그아웃(세션 무효화)
        try {
            session.invalidate();
        } catch (Exception e) {
            // invalidate 실패를 500으로 처리해도 되지만 예외가 발생할 수도 있음
            // 여기서는 간단히 BusinessException으로 처리하는 것이 좋으므로 AuthService.logout(session) 호출하는 것이 좋음
            throw e;
        }

        return ApiResponse.success(WithdrawResponse.from(true));
    }
}
