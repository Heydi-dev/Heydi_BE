package com.example.heydibe.diary.dto.response;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.example.heydibe.diary.entity.Diary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiariesResponse {
    private List<DiaryResponse> content;
    private Integer page;
    private Integer size;
    private int totalElements;
    private int totalPages;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiaryResponse {
        private Long diaryId;
        private String date;
        private String title;
        private List<String> topics;
        private String emotion;

        public static DiaryResponse from(Diary diary) {
            DiaryResponse response = new DiaryResponse();
            response.setDiaryId(diary.getId());

            String strDate = diary.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            response.setDate(strDate);
            response.setTitle(diary.getTitle());

            List<String> topics = new ArrayList<>();
            if (diary.getTopic1() != null) {
                topics.add(diary.getTopic1());
            }
            if (diary.getTopic2() != null) {
                topics.add(diary.getTopic2());
            }
            response.setTopics(topics);

            response.setEmotion(diary.getMainEmotion());
            return response;
        }
    }
}
