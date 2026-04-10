package com.example.heydibe.report.service;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.report.domain.MonthlyReport;
import com.example.heydibe.report.dto.MonthlyReportApiDto.*;
import com.example.heydibe.report.repository.MonthlyReportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlyReportQueryService {

    private static final Pattern YEAR_MONTH_PATTERN = Pattern.compile("^\\d{4}-\\d{2}$");

    private final MonthlyReportRepository monthlyReportRepository;

    public AvailableMonthsResult getAvailableMonths(Long userId) {
        validateUserId(userId);

        List<MonthlyReport> reports = monthlyReportRepository.findByUserIdOrderByReportYearMonthDesc(userId);
        List<String> availableMonths = reports.stream()
                .map(MonthlyReport::getReportYearMonth)
                .toList();

        return AvailableMonthsResult.builder()
                .availableMonths(availableMonths)
                .defaultYearMonth(availableMonths.isEmpty() ? null : availableMonths.get(0))
                .build();
    }

    public MonthlyReportResult getMonthlyReport(Long userId, String yearMonth) {
        MonthlyReport report = getReport(userId, yearMonth);
        JsonNode root = report.getAnalysisJson();

        return MonthlyReportResult.builder()
                .yearMonth(report.getReportYearMonth())
                .preferences(buildPreferences(root))
                .activity(Activity.builder()
                        .summary(text(root, "summary"))
                        .build())
                .insight(Insight.builder()
                        .content(text(root, "monthlyInsight"))
                        .build())
                .lastMonthReminder(buildLastMonthReminder(root))
                .build();
    }

    public CalendarResult getCalendar(Long userId, String yearMonth) {
        MonthlyReport report = getReport(userId, yearMonth);
        JsonNode root = report.getAnalysisJson();

        JsonNode entriesNode = path(root, "calendar", "entries");
        List<CalendarEntry> entries = new ArrayList<>();

        if (entriesNode != null && entriesNode.isArray()) {
            for (JsonNode node : entriesNode) {
                entries.add(CalendarEntry.builder()
                        .date(text(node, "date"))
                        .diaryId(longValue(node, "diaryId"))
                        .build());
            }
        }

        return CalendarResult.builder()
                .entries(entries)
                .build();
    }

    public TopicsResult getTopics(Long userId, String yearMonth) {
        MonthlyReport report = getReport(userId, yearMonth);
        JsonNode root = report.getAnalysisJson();

        JsonNode topicsNode = root != null ? root.get("topics") : null;

        if (topicsNode != null && topicsNode.isObject()) {
            return TopicsResult.builder()
                    .yearMonth(report.getReportYearMonth())
                    .top1(buildTop1(topicsNode.get("top1")))
                    .top2to4(buildTop2to4(topicsNode.get("top2to4")))
                    .build();
        }

        JsonNode keywordsNode = root != null ? root.get("keywords") : null;
        List<TopicRank> keywordRanks = buildTopicRanksFromKeywords(keywordsNode);

        TopicDetail top1 = null;
        List<TopicRank> top2to4 = Collections.emptyList();

        if (!keywordRanks.isEmpty()) {
            TopicRank first = keywordRanks.get(0);
            top1 = TopicDetail.builder()
                    .name(first.getName())
                    .ratio(first.getRatio())
                    .description(null)
                    .build();

            if (keywordRanks.size() > 1) {
                top2to4 = keywordRanks.subList(1, Math.min(keywordRanks.size(), 4));
            }
        }

        return TopicsResult.builder()
                .yearMonth(report.getReportYearMonth())
                .top1(top1)
                .top2to4(top2to4)
                .build();
    }

    public EmotionsResult getEmotions(Long userId, String yearMonth) {
        MonthlyReport report = getReport(userId, yearMonth);
        JsonNode root = report.getAnalysisJson();

        JsonNode weeksNode = root != null ? root.get("weeks") : null;
        List<EmotionWeek> weeks = new ArrayList<>();

        if (weeksNode != null && weeksNode.isArray()) {
            for (JsonNode node : weeksNode) {
                weeks.add(EmotionWeek.builder()
                        .weekIndex(intValue(node, "weekIndex"))
                        .startDate(text(node, "startDate"))
                        .endDate(text(node, "endDate"))
                        .topEmotion(text(node, "topEmotion"))
                        .emotionRate(intValue(node, "emotionRate"))
                        .build());
            }
        } else {
            JsonNode emotionStats = root != null ? root.get("emotionStats") : null;
            if (emotionStats != null && emotionStats.isArray()) {
                int idx = 1;
                for (JsonNode node : emotionStats) {
                    weeks.add(EmotionWeek.builder()
                            .weekIndex(idx++)
                            .startDate(text(node, "startDate"))
                            .endDate(text(node, "endDate"))
                            .topEmotion(
                                    text(node, "topEmotion") != null
                                            ? text(node, "topEmotion")
                                            : text(root, "topEmotion")
                            )
                            .emotionRate(intValue(node, "emotionRate"))
                            .build());
                }
            }
        }

        return EmotionsResult.builder()
                .yearMonth(report.getReportYearMonth())
                .weeks(weeks)
                .build();
    }

    private MonthlyReport getReport(Long userId, String yearMonth) {
        validateUserId(userId);
        validateYearMonth(yearMonth);

        return monthlyReportRepository.findByUserIdAndReportYearMonth(userId, yearMonth)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

    private Preferences buildPreferences(JsonNode root) {
        JsonNode preferencesNode = root != null ? root.get("preferences") : null;

        if (preferencesNode != null && preferencesNode.isObject()) {
            return Preferences.builder()
                    .like(text(preferencesNode, "like"))
                    .dislike(text(preferencesNode, "dislike"))
                    .build();
        }

        return Preferences.builder()
                .like(null)
                .dislike(null)
                .build();
    }

    private LastMonthReminder buildLastMonthReminder(JsonNode root) {
        JsonNode node = root != null ? root.get("lastMonthReminder") : null;

        if (node == null || node.isNull()) {
            return null;
        }

        return LastMonthReminder.builder()
                .sourceYearMonth(text(node, "sourceYearMonth"))
                .diaryId(longValue(node, "diaryId"))
                .date(text(node, "date"))
                .title(text(node, "title"))
                .topics(stringList(node.get("topics")))
                .emotion(text(node, "emotion"))
                .build();
    }

    private TopicDetail buildTop1(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }

        return TopicDetail.builder()
                .name(text(node, "name"))
                .ratio(intValue(node, "ratio"))
                .description(text(node, "description"))
                .build();
    }

    private List<TopicRank> buildTop2to4(JsonNode node) {
        if (node == null || !node.isArray()) {
            return Collections.emptyList();
        }

        List<TopicRank> result = new ArrayList<>();
        for (JsonNode item : node) {
            result.add(TopicRank.builder()
                    .name(text(item, "name"))
                    .ratio(intValue(item, "ratio"))
                    .build());
        }
        return result;
    }

    private List<TopicRank> buildTopicRanksFromKeywords(JsonNode node) {
        if (node == null || !node.isArray()) {
            return Collections.emptyList();
        }

        List<TopicRank> result = new ArrayList<>();
        for (JsonNode item : node) {
            if (item.isTextual()) {
                result.add(TopicRank.builder()
                        .name(item.asText())
                        .ratio(null)
                        .build());
            } else {
                result.add(TopicRank.builder()
                        .name(text(item, "name"))
                        .ratio(intValue(item, "ratio"))
                        .build());
            }
        }
        return result;
    }

    private JsonNode path(JsonNode root, String... keys) {
        JsonNode current = root;
        for (String key : keys) {
            if (current == null) {
                return null;
            }
            current = current.get(key);
        }
        return current;
    }

    private String text(JsonNode node, String key) {
        if (node == null) return null;
        JsonNode child = node.get(key);
        return (child == null || child.isNull()) ? null : child.asText();
    }

    private Integer intValue(JsonNode node, String key) {
        if (node == null) return null;
        JsonNode child = node.get(key);
        return (child == null || child.isNull()) ? null : child.asInt();
    }

    private Long longValue(JsonNode node, String key) {
        if (node == null) return null;
        JsonNode child = node.get(key);
        return (child == null || child.isNull()) ? null : child.asLong();
    }

    private List<String> stringList(JsonNode node) {
        if (node == null || !node.isArray()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        for (JsonNode item : node) {
            result.add(item.asText());
        }
        return result;
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new CustomException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
    }

    private void validateYearMonth(String yearMonth) {
        if (yearMonth == null || !YEAR_MONTH_PATTERN.matcher(yearMonth).matches()) {
            throw new CustomException(ErrorCode.REPORT_YEAR_MONTH_INVALID);
        }
    }
}