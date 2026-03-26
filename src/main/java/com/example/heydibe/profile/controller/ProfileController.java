package com.example.heydibe.profile.controller;

import com.example.heydibe.auth.service.AuthService;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.profile.dto.request.ProfileUpdateRequest;
import com.example.heydibe.profile.dto.response.ProfileResponse;
import com.example.heydibe.profile.dto.response.ProfileUpdateResponse;
import com.example.heydibe.profile.service.ProfileService;
import com.example.heydibe.user.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage/profile")
public class ProfileController {

    private final AuthService authService;
    private final ProfileService profileService;

    @GetMapping("/me")
    public ApiResponse<ProfileResponse> getMyProfile(HttpSession session) {
        User user = authService.getLoginUserFromSession(session);
        return ApiResponse.success("프로필 조회 성공", profileService.getMyProfile(user.getId()));
    }

    @PatchMapping(value = "/me", consumes = "multipart/form-data")
    public ApiResponse<ProfileUpdateResponse> updateProfile(
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestParam(value = "currentPassword", required = false) String currentPassword,
            @RequestParam(value = "newPassword", required = false) String newPassword,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "deleteProfileImage", required = false) String deleteProfileImage,
            HttpSession session) {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setProfileImage(profileImage);
        request.setCurrentPassword(currentPassword);
        request.setNewPassword(newPassword);
        request.setNickname(nickname);
        request.setDeleteProfileImage(deleteProfileImage);
        
        User user = authService.getLoginUserFromSession(session);
        return ApiResponse.success("프로필 수정 성공", profileService.updateProfile(user.getId(), request));
    }
}
