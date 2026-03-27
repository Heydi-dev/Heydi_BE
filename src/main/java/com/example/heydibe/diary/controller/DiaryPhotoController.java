package com.example.heydibe.diary.controller;

import com.example.heydibe.auth.service.AuthService;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.diary.dto.request.DiaryPhotoUploadRequest;
import com.example.heydibe.diary.dto.response.DiaryPhotoListResponse;
import com.example.heydibe.diary.dto.response.DiaryPhotoUploadResponse;
import com.example.heydibe.diary.service.DiaryPhotoService;
import com.example.heydibe.user.entity.User;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries")
public class DiaryPhotoController {

    private final AuthService authService;
    private final DiaryPhotoService diaryPhotoService;

    @PostMapping(value = "/{diaryId}/photos", consumes = "multipart/form-data")
    public ApiResponse<DiaryPhotoUploadResponse> uploadPhoto(
            @PathVariable Long diaryId,
            @ModelAttribute @Valid DiaryPhotoUploadRequest request,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);
        DiaryPhotoUploadResponse response = diaryPhotoService.createPhotos(user.getId(), diaryId, request.getPhotos());
        return ApiResponse.success("다이어리 사진 업로드 성공", response);
    }

    @PutMapping(value = "/{diaryId}/photos", consumes = "multipart/form-data")
    public ApiResponse<DiaryPhotoUploadResponse> updatePhotos(
            @PathVariable Long diaryId,
            @ModelAttribute @Valid DiaryPhotoUploadRequest request,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);
        DiaryPhotoUploadResponse response = diaryPhotoService.replacePhotos(user.getId(), diaryId, request.getPhotos());
        return ApiResponse.success("다이어리 사진 수정 성공", response);
    }

    @DeleteMapping("/{diaryId}/photos/{fileId}")
    public ApiResponse<Void> deletePhoto(
            @PathVariable Long diaryId,
            @PathVariable Long fileId,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);
        diaryPhotoService.deletePhoto(user.getId(), diaryId, fileId);
        return ApiResponse.success("다이어리 사진 삭제 성공", null);
    }

    @GetMapping("/{diaryId}/photos")
    public ApiResponse<DiaryPhotoListResponse> getPhotos(
            @PathVariable Long diaryId,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);
        DiaryPhotoListResponse response = diaryPhotoService.getPhotos(user.getId(), diaryId);
        return ApiResponse.success("다이어리 사진 조회 성공", response);
    }
}
