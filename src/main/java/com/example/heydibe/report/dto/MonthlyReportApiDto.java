package com.example.heydibe.report.dto;

import java.util.List;

/**
 * API 명세서의 response.result 구조를 그대로 반영한 DTO 모음
 */
public class MonthlyReportApiDto {

    public record AvailableMonthsResult(
            List<String> availableMonths,
            String defaultYearMonth
    ) {}

    public record CalendarResult(
            String yearMonth,
            List<CalendarEntry> entries
    ) {}

    public record CalendarEntry(
            String date,
            Long diaryId
    ) {}

    public record InsightsResult(
            String yearMonth,
            String summary,
            List<String> highlights,
            List<String> improvements,
            String encouragement
    ) {}

    public record ActivitiesResult(
            String yearMonth,
            List<ActivityItem> activities
    ) {}

    public record ActivityItem(
            String name,
            int count,
            int ratio
    ) {}

    public record PreferencesResult(
            String yearMonth,
            PreferenceBlock like,
            PreferenceBlock dislike
    ) {}

    public record PreferenceBlock(
            String title,
            String keyword,
            String description
    ) {}

    public record TopicsResult(
            String yearMonth,
            List<TopicItem> topics
    ) {}

    public record TopicItem(
            String name,
            int count,
            double weight
    ) {}

    public record EmotionsResult(
            String yearMonth,
            List<WeekEmotion> weeks
    ) {}

    public record WeekEmotion(
            int weekIndex,
            String startDate,
            String endDate,
            String topEmotion,
            int emotionRate
    ) {}

    public record ReminderResult(
            String baseYearMonth,
            String sourceYearMonth,
            Long diaryId,
            String date,
            String title,
            String topic,
            String mainEmotion
    ) {}
}
