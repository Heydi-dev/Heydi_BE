package com.example.heydibe.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class MonthlyReportApiDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailableMonthsResult {
        private List<String> availableMonths;
        private String defaultYearMonth;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyReportResult {
        private String yearMonth;
        private Preferences preferences;
        private Activity activity;
        private Insight insight;
        private LastMonthReminder lastMonthReminder;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Preferences {
        private String like;
        private String dislike;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Activity {
        private String summary;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Insight {
        private String content;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LastMonthReminder {
        private String sourceYearMonth;
        private Long diaryId;
        private String date;
        private String title;
        private List<String> topics;
        private String emotion;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarResult {
        private List<CalendarEntry> entries;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarEntry {
        private String date;
        private Long diaryId;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicsResult {
        private String yearMonth;
        private TopicDetail top1;
        private List<TopicRank> top2to4;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicDetail {
        private String name;
        private Integer ratio;
        private String description;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicRank {
        private String name;
        private Integer ratio;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionsResult {
        private String yearMonth;
        private List<EmotionWeek> weeks;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionWeek {
        private Integer weekIndex;
        private String startDate;
        private String endDate;
        private String topEmotion;
        private Integer emotionRate;
    }
}