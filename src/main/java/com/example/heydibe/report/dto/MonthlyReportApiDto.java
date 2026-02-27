package com.example.heydibe.report.dto;

import java.util.List;

public class MonthlyReportApiDto {

    // A) GET /reports/monthly
    public record AvailableMonthsResult(
            List<String> availableMonths,
            String defaultYearMonth
    ) {}

    // B) GET /reports/monthly/{yearMonth}/calendar  (yearMonth 필드 제거됨)
    public record CalendarResult(
            List<CalendarEntry> entries
    ) {}

    public record CalendarEntry(
            String date,
            Long diaryId
    ) {}

    // ✅ 통합 조회: GET /reports/monthly/{yearMonth}
    public record MonthlyReportUnifiedResult(
            String yearMonth,
            Preferences preferences,
            Activity activity,
            Insight insight,
            LastMonthReminder lastMonthReminder
    ) {}

    public record Preferences(String like, String dislike) {}
    public record Activity(String summary) {}
    public record Insight(String content) {}

    public record LastMonthReminder(
            String sourceYearMonth,
            Long diaryId,
            String date,
            String title,
            List<String> topics,
            String emotion
    ) {}

    // ✅ topics 변경: top2~4도 ratio 표시
    public record TopicsResult(
            String yearMonth,
            TopTopic top1,
            List<SubTopic> top2to4
    ) {}

    public record TopTopic(
            String name,
            int ratio,
            String description
    ) {}

    public record SubTopic(
            String name,
            int ratio
    ) {}
}