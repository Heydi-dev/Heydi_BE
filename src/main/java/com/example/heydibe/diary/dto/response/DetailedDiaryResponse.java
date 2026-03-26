package com.example.heydibe.diary.dto.response;

import java.util.List;

import com.example.heydibe.diary.entity.DiaryAttachment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetailedDiaryResponse {
    private Long id;
    private String title;
    private String createdDate; // "2025-11-21T21:23:00"
    private String emotionCategory;
    private List<String> topic;
    private String oneLineDiary;
    private String content;
    private String conversationSessionId;
    private int conversationDurationSec;
    private List<PhotoResponse> photos;
    private ReportResponse report;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhotoResponse {
        private Long id;
        private String imageUrl;
        private int order;

        public static PhotoResponse from(DiaryAttachment attachment) {
            return new PhotoResponse(attachment.getId(), attachment.getFileUrl(), 0);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportResponse {
        private boolean included;
        private String month; // "2025-11"
    }
}
