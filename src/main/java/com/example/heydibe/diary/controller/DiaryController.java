package com.example.heydibe.diary.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.heydibe.ai.dto.response.TestResponse;
import com.example.heydibe.auth.service.AuthService;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.diary.dto.response.DetailedDiaryResponse;
import com.example.heydibe.diary.dto.response.DiariesResponse;
import com.example.heydibe.diary.service.DiaryService;
import com.example.heydibe.user.entity.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries")
public class DiaryController {
    private final DiaryService diaryService;
    private final AuthService authService;

    @GetMapping("/test")
    public ApiResponse<TestResponse> getTest() {
        return ApiResponse.success("AI 서버 연결 테스트 성공", diaryService.getTest());
    }

    @GetMapping
    public DiariesResponse getDiaries(@RequestParam Integer pageNumber,
            @RequestParam Integer pageSize, HttpSession session) {
        User user = authService.getLoginUserFromSession(session);
        return diaryService.getDiaries(user, pageNumber, pageSize);
    }

    // is_public==true일 경우 전체 조회, false일 경우 본인만 조회 가능.
    @GetMapping("/{diaryId}")
    public DetailedDiaryResponse getDiaryById(@PathVariable Long diaryId) {
        return diaryService.getDiaryById(diaryId);
    }

    @PutMapping("/{diaryId}")
    public String putDiary(@PathVariable Long diaryId, @RequestBody String entity) {
        return "put diary by id: " + diaryId + " with entity: " + entity + " - TODO";
    }

    @DeleteMapping("/{diaryId}")
    public String deleteDiary(@PathVariable Long diaryId) {
        return "delete diary by id: " + diaryId + " - TODO";
    }

    @GetMapping("/{diaryId}/conversation")
    public String getConversation(@PathVariable Long diaryId, @RequestParam String param) {
        return "get conversation for diary id: " + diaryId + " with param: " + param + " - TODO";
    }

    @GetMapping("/{diaryId}/photos")
    public String getPhotos(@PathVariable Long diaryId) {
        return "get photos for diary id: " + diaryId + " - TODO";
    }

    @PostMapping("/{diaryId}/photos")
    public String postPhotos(@PathVariable Long diaryId, @RequestBody String entity) {

        return "posted photos for diary id: " + diaryId + " with entity: " + entity + " - TODO";
    }

    @DeleteMapping("/{diaryId}/photos/{photoId}")
    public String deletePhotos(@PathVariable Long diaryId, @PathVariable Long photoId) {
        return "deleted photo id: " + photoId + " for diary id: " + diaryId + " - TODO";
    }

    @PostMapping("/{diaryId}/export/pdf")
    public String exportPdf(@PathVariable Long diaryId) {
        return "exported pdf for diary id: " + diaryId + " - TODO";
    }
}
