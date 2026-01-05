package com.example.heydibe.report.dto;

import java.util.List;

public class MonthlyReportApiDto {

    /* ---------- A ---------- */
    public record AvailableMonthsResult(
            List<String> availableMonths,
            String defaultYearMonth
    ) {}

    /* ---------- B ---------- */
    public record CalendarEntry(
            String date,
            Long diaryId
    ) {}

    public record CalendarResult(
            String yearMonth,
            List<CalendarEntry> entries
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

    /* ---------- C ---------- */
    public record InsightsResult(
            String yearMonth,
            String summary,
            List<String> highlights,
            List<String> improvements,
            String encouragement
    ) {}

    public record ActivityItem(
            String name,
            int count,
            int ratio
    ) {}

    public record ActivitiesResult(
            String yearMonth,
            List<ActivityItem> activities
    ) {}

    public record PreferenceBlock(
            String title,
            String keyword,
            String description
    ) {}

    public record PreferencesResult(
            String yearMonth,
            PreferenceBlock like,
            PreferenceBlock dislike
    ) {}

    public record TopicItem(
            String name,
            int count,
            double weight
    ) {}

    public record TopicsResult(
            String yearMonth,
            List<TopicItem> topics
    ) {}

    public record WeekEmotion(
            int weekIndex,
            String startDate,
            String endDate,
            String topEmotion,
            int emotionRate
    ) {}

    public record EmotionsResult(
            String yearMonth,
            List<WeekEmotion> weeks
    ) {}

    /* ---------- PUT ---------- */
    // 🔥 여기 핵심 변경 포인트
    public record MonthlyReportUpsertRequest(
            Object analysis   // ❌ JsonNode 쓰지 말 것
    ) {}

    public record MonthlyReportUpsertResult(
            String yearMonth
    ) {}
}
