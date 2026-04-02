package com.example.heydibe.diary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ConversationSessionEndResponse {
    private String status;
    private String endedAt;
    private DiarySummary diary;

    @Getter
    @AllArgsConstructor
    public static class DiarySummary {
        private Long id;
        private String date;
        private String title;
        private String emotionText;
        private String emotionCategory;
        private List<String> topic;
        private String oneLineDiary;
        private String content;
    }
}
