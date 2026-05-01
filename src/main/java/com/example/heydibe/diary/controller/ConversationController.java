package com.example.heydibe.diary.controller;

import com.example.heydibe.auth.service.AuthService;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.diary.dto.request.ConversationSessionEndRequest;
import com.example.heydibe.diary.dto.request.ConversationSessionStartRequest;
import com.example.heydibe.diary.dto.response.ConversationMessageHistoryResponse;
import com.example.heydibe.diary.dto.response.ConversationSessionEndResponse;
import com.example.heydibe.diary.dto.response.ConversationSessionStartResponse;
import com.example.heydibe.diary.service.ConversationSessionService;
import com.example.heydibe.user.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
@RequiredArgsConstructor
public class ConversationController {

    private final AuthService authService;
    private final ConversationSessionService conversationSessionService;

    @PostMapping("/api/conversations/sessions")
    public ResponseEntity<ApiResponse<ConversationSessionStartResponse>> startSession(
            @RequestBody(required = false) ConversationSessionStartRequest request,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);
        ConversationSessionStartResponse response = conversationSessionService.startSession(user.getId(), request);
        return ResponseEntity.status(201)
                .body(ApiResponse.success(201, "Conversation session started", response));
    }

    @GetMapping("/api/conversations/sessions/{diaryId}/messages")
    public ApiResponse<ConversationMessageHistoryResponse> getMessages(
            @PathVariable("diaryId") Long diaryId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);
        ConversationMessageHistoryResponse response =
                conversationSessionService.getMessageHistory(user.getId(), diaryId, page, size);
        return ApiResponse.success("Conversation message history fetched", response);
    }

    @PostMapping("/api/conversations/sessions/{diaryId}/end")
    public ApiResponse<ConversationSessionEndResponse> endSession(
            @PathVariable("diaryId") Long diaryId,
            @RequestBody(required = false) ConversationSessionEndRequest request,
            HttpSession session
    ) {
        User user = authService.getLoginUserFromSession(session);
        ConversationSessionEndResponse response = conversationSessionService.endSession(user.getId(), diaryId, request);
        return ApiResponse.success("Session ended and diary generated", response);
    }
}
