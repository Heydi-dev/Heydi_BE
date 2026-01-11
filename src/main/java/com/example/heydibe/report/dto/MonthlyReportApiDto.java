package com.example.heydibe.report.dto;

import java.util.List;

public class MonthlyReportApiDto {

    // GET /reports/monthly
    public record AvailableMonthsResult(
            List<String> availableMonths,
            String defaultYearMonth
    ) {}

    // GET /{yearMonth}/reminder
    public record ReminderResult(
            String baseYearMonth,
            String sourceYearMonth,
            Long diaryId,
            String date,
            String title,
            String topic,
            String mainEmotion
    ) {}

    // GET /{yearMonth}/calendar
    public record CalendarResult(
            String yearMonth,
            List<CalendarEntry> entries
    ) {}

    public record CalendarEntry(
            String date,   // yyyy-MM-dd
            Long diaryId
    ) {}

    // GET /{yearMonth}/insights
    public record InsightsResult(
            String yearMonth,
            String summary,
            List<String> highlights,
            List<String> improvements,
            String encouragement
    ) {}

    // GET /{yearMonth}/activities
    public record ActivitiesResult(
            String yearMonth,
            List<ActivityItem> activities
    ) {}

    public record ActivityItem(
            String name,
            int count,
            int ratio
    ) {}

    // GET /{yearMonth}/preferences
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

    // GET /{yearMonth}/topics
    public record TopicsResult(
            String yearMonth,
            List<TopicItem> topics
    ) {}

    public record TopicItem(
            String name,
            int count,
            double weight
    ) {}

    // GET /{yearMonth}/emotions
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

    // PUT /{yearMonth}
    public record MonthlyReportUpsertRequest(
            Object analysis
    ) {}

    public record MonthlyReportUpsertResult(
            String yearMonth
    ) {}
}
