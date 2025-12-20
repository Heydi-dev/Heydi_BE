package com.example.demo.user.controller;

import com.example.demo.auth.AuthDto;
import com.example.demo.auth.service.AuthService;
import com.example.demo.common.ApiResponse;
import com.example.demo.user.request.SignupRequest;
import com.example.demo.user.response.SignupResponse;
import com.example.demo.user.response.WithdrawResponse;
import com.example.demo.user.service.UserService;
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
            // invalidate 실패는 500으로 처리되도록 예외로 던져도 됨
            // 여기서는 간단히 BusinessException으로 넘기고 싶으면 AuthService.logout(session) 호출로 대체 가능
            throw e;
        }

        return ApiResponse.success(WithdrawResponse.from(true));
    }
}
