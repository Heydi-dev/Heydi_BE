package com.example.heydibe.diary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConversationSessionStartResponse {
    private Long diaryId;
    private String date;
    private String startedAt;
    private String status;
}
