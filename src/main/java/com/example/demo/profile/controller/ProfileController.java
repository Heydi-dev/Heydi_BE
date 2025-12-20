package com.example.demo.profile.controller;

import com.example.demo.auth.AuthDto;
import com.example.demo.auth.service.AuthService;
import com.example.demo.common.ApiResponse;
import com.example.demo.profile.request.ProfileUpdateRequest;
import com.example.demo.profile.response.UserProfileResponse;
import com.example.demo.profile.service.ProfileService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final AuthService authService;
    private final ProfileService profileService;

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMyProfile(HttpSession session) {
        AuthDto auth = authService.getLoginUserFromSession(session);
        return ApiResponse.success(profileService.getMyProfile(auth.getUserId()));
    }

    @PutMapping("/me")
    public ApiResponse<Void> updateProfile(@Valid @RequestBody ProfileUpdateRequest request, HttpSession session) {
        AuthDto auth = authService.getLoginUserFromSession(session);
        profileService.updateProfile(auth.getUserId(), request);
        return ApiResponse.success(null);
    }
}
