package com.example.heydibe.report.dto;

import java.util.List;

public class MonthlyReportApiDto {

    // ------------------------
    // GET /reports/monthly
    // ------------------------
    public record AvailableMonthsResult(
            List<String> availableMonths,
            String defaultYearMonth
    ) {}

    // ------------------------
    // GET /reports/monthly/{yearMonth}/topics
    // ------------------------
    public record TopicsResult(
            String yearMonth,
            Top1 top1,
            List<String> top2to4
    ) {
        public record Top1(
                String name,
                int ratio,
                String description
        ) {}
    }

    // ------------------------
    // GET /reports/monthly/{yearMonth}/preferences
    // ------------------------
    public record PreferencesResult(
            String yearMonth,
            String like,
            String dislike
    ) {}

    // ------------------------
    // GET /reports/monthly/{yearMonth}/activities
    // ------------------------
    public record ActivitiesResult(
            String yearMonth,
            String summary
    ) {}

    // ------------------------
    // GET /reports/monthly/{yearMonth}/insights
    // ------------------------
    public record InsightsResult(
            String yearMonth,
            String insight
    ) {}

    // ------------------------
    // GET /reports/monthly/{yearMonth}/calendar
    // ------------------------
    public record CalendarResult(
            List<CalendarEntry> entries
    ) {
        public record CalendarEntry(
                String date,
                Long diaryId
        ) {}
    }

    // ------------------------
    // GET /reports/monthly/{yearMonth}/reminder
    // ------------------------
    public record ReminderResult(
            String baseYearMonth,
            String sourceYearMonth,
            Long diaryId,
            String date,
            String title,
            String topic,
            String emotion
    ) {}
}
