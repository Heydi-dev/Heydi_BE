package com.example.heydibe.report.service;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.report.domain.MonthlyReport;
import com.example.heydibe.report.dto.MonthlyReportApiDto.*;
import com.example.heydibe.report.repository.MonthlyReportRepository;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class MonthlyReportQueryService {

    private final MonthlyReportRepository monthlyReportRepository;

    public MonthlyReportQueryService(MonthlyReportRepository monthlyReportRepository) {

        this.monthlyReportRepository = monthlyReportRepository;
    }

    // A) /reports/monthly
    public AvailableMonthsResult getAvailableMonths(Long userId) {
        try {
            List<String> months = monthlyReportRepository.findAvailableMonths(userId);
            String defaultYm = monthlyReportRepository.findDefaultYearMonth(userId);
            return new AvailableMonthsResult(months, defaultYm);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.REPORT_MONTH_LIST_FETCH_FAILED);
        }
    }

    // ✅ 통합: /reports/monthly/{yearMonth}
    public MonthlyReportUnifiedResult getUnified(Long userId, String yearMonth) {
        YearMonth ym = parseYearMonthOrThrow(yearMonth);

        try {
            JsonNode root = readAnalysisRoot(userId, ym.toString());

            // preferences
            JsonNode pref = root.path("preferences");
            Preferences preferences = new Preferences(
                    textOrNull(pref, "like"),
                    textOrNull(pref, "dislike")
            );

            // activity
            JsonNode activityNode = root.path("activity");
            Activity activity = new Activity(
                    textOrNull(activityNode, "summary")
            );

            // insight
            JsonNode insightNode = root.path("insight");
            Insight insight = new Insight(
                    textOrNull(insightNode, "content")
            );

            // reminder
            JsonNode rem = root.path("lastMonthReminder");
            List<String> topics = stringArrayOrEmpty(rem.path("topics"));
            LastMonthReminder reminder = new LastMonthReminder(
                    textOrNull(rem, "sourceYearMonth"),
                    longOrNull(rem, "diaryId"),
                    textOrNull(rem, "date"),
                    textOrNull(rem, "title"),
                    topics,
                    textOrNull(rem, "emotion")
            );

            return new MonthlyReportUnifiedResult(
                    yearMonth,
                    preferences,
                    activity,
                    insight,
                    reminder
            );
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.REPORT_FETCH_FAILED);
        }
    }

    // ✅ topics: /reports/monthly/{yearMonth}/topics
    public TopicsResult getTopics(Long userId, String yearMonth) {
        YearMonth ym = parseYearMonthOrThrow(yearMonth);

        try {
            JsonNode root = readAnalysisRoot(userId, ym.toString());
            JsonNode arr = root.path("topics");
            if (!arr.isArray()) {
                throw new CustomException(ErrorCode.REPORT_TOPICS_FETCH_FAILED);
            }

            List<JsonNode> list = new ArrayList<>();
            for (JsonNode n : arr) list.add(n);

            if (list.isEmpty()) {
                throw new CustomException(ErrorCode.REPORT_TOPICS_FETCH_FAILED);
            }

            JsonNode first = list.get(0);
            TopTopic top1 = new TopTopic(
                    first.path("name").asText(""),
                    first.path("ratio").asInt(0),
                    first.path("description").asText(null)
            );

            List<SubTopic> top2to4 = new ArrayList<>();
            for (int i = 1; i < Math.min(list.size(), 4); i++) {
                JsonNode n = list.get(i);
                top2to4.add(new SubTopic(
                        n.path("name").asText(""),
                        n.path("ratio").asInt(0)
                ));
            }

            if (top1.name() == null || top1.name().isBlank()) {
                throw new CustomException(ErrorCode.REPORT_TOPICS_FETCH_FAILED);
            }

            return new TopicsResult(yearMonth, top1, top2to4);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.REPORT_TOPICS_FETCH_FAILED);
        }
    }

    // B) calendar (명세가 entries만)
    public CalendarResult getCalendar(Long userId, String yearMonth) {
        parseYearMonthOrThrow(yearMonth);
        try {
            return new CalendarResult(List.of());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.REPORT_CALENDAR_FETCH_FAILED);
        }
    }

    // ---------------------
    // helpers
    // ---------------------
    private YearMonth parseYearMonthOrThrow(String yearMonth) {
        try {
            return YearMonth.parse(yearMonth);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_YEAR_MONTH_FORMAT);
        }
    }

    private JsonNode readAnalysisRoot(Long userId, String yearMonth) {
        MonthlyReport mr = monthlyReportRepository.findByUserIdAndReportYearMonth(userId, yearMonth)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_MONTH_LIST_FETCH_FAILED));

        JsonNode json = mr.getAnalysisJson();
        return (json == null || json.isNull()) ? JsonNodeFactory.instance.objectNode() : json;
    }

    private String textOrNull(JsonNode node, String field) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        JsonNode v = node.path(field);
        if (v.isMissingNode() || v.isNull()) return null;
        String s = v.asText();
        return (s == null || s.isBlank()) ? null : s;
    }

    private Long longOrNull(JsonNode node, String field) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        JsonNode v = node.path(field);
        if (v.isMissingNode() || v.isNull()) return null;
        if (!v.canConvertToLong()) return null;
        return v.asLong();
    }

    private List<String> stringArrayOrEmpty(JsonNode arr) {
        if (arr == null || !arr.isArray()) return List.of();
        List<String> out = new ArrayList<>();
        for (JsonNode n : arr) {
            String s = n.asText(null);
            if (s != null && !s.isBlank()) out.add(s);
        }
        return out;
    }
}