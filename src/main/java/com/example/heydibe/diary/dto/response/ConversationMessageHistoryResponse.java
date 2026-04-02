package com.example.heydibe.diary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ConversationMessageHistoryResponse {
    private List<DiaryConversationResponse.MessageResponse> messages;
}
