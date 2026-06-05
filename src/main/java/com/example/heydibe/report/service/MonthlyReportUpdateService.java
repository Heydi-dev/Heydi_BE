package com.example.heydibe.report.service;

import com.example.heydibe.ai.service.AiService;
import com.example.heydibe.diary.entity.Diary;
import com.example.heydibe.diary.repository.DiaryRepository;
import com.example.heydibe.report.domain.MonthlyReport;
import com.example.heydibe.report.repository.MonthlyReportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonthlyReportUpdateService {
    private static final String STATUS_ENDED = "ENDED";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final DiaryRepository diaryRepository;
    private final MonthlyReportRepository monthlyReportRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    @Value("${app.report.monthly.include-only-marked:false}")
    private boolean includeOnlyMarked;

    public void updateReportForDiary(Diary diary) {
        if (diary == null || diary.getUser() == null) {
            return;
        }

        LocalDate diaryDate = resolveDiaryDate(diary);
        if (diaryDate == null) {
            return;
        }

        updateReport(diary.getUser().getId(), YearMonth.from(diaryDate));
    }

    public void updateReport(Long userId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Diary> diaries = diaryRepository
                .findByUser_IdAndDeletedAtIsNullAndDiaryDateBetweenOrderByDiaryDateAsc(userId, startDate, endDate)
                .stream()
                .filter(this::isReportableDiary)
                .toList();

        if (diaries.isEmpty()) {
            return;
        }

        List<Map<String, String>> aiEntries = buildAiEntries(diaries);
        JsonNode preferencesResponse = aiService.generateMonthlyPreferences(aiEntries);
        String activityComment = aiService.generateMonthlyActivityComment(aiEntries);
        String feedbackComment = aiService.generateMonthlyFeedbackComment(aiEntries);

        ObjectNode analysisJson = objectMapper.createObjectNode();
        analysisJson.set("preferences", buildPreferences(preferencesResponse));
        analysisJson.set("preferenceDetails", preferencesResponse.deepCopy());
        analysisJson.put("summary", activityComment);
        analysisJson.put("monthlyInsight", feedbackComment);
        analysisJson.set("lastMonthReminder", buildLastMonthReminder(userId, yearMonth));
        analysisJson.set("calendar", buildCalendar(diaries));
        analysisJson.set("topics", buildTopics(diaries));
        analysisJson.set("weeks", buildWeeks(yearMonth, diaries));

        String reportYearMonth = yearMonth.format(YEAR_MONTH_FORMATTER);
        MonthlyReport report = monthlyReportRepository.findByUserIdAndReportYearMonth(userId, reportYearMonth)
                .orElseGet(() -> new MonthlyReport(userId, reportYearMonth, analysisJson));

        report.updateAnalysisJson(analysisJson);
        monthlyReportRepository.save(report);
    }

    private List<Map<String, String>> buildAiEntries(List<Diary> diaries) {
        List<Map<String, String>> entries = new ArrayList<>();
        for (Diary diary : diaries) {
            LocalDate diaryDate = resolveDiaryDate(diary);
            if (diaryDate == null) {
                continue;
            }

            entries.add(Map.of(
                    "date", diaryDate.format(DATE_FORMATTER),
                    "text", diary.getContent().trim()
            ));
        }
        return entries;
    }

    private ObjectNode buildPreferences(JsonNode preferencesResponse) {
        ObjectNode preferences = objectMapper.createObjectNode();
        preferences.put("like", readText(preferencesResponse, "like", "keyword"));
        preferences.put("dislike", readText(preferencesResponse, "dislike", "keyword"));
        return preferences;
    }

    private ObjectNode buildCalendar(List<Diary> diaries) {
        ObjectNode calendar = objectMapper.createObjectNode();
        ArrayNode entries = objectMapper.createArrayNode();

        for (Diary diary : diaries) {
            LocalDate diaryDate = resolveDiaryDate(diary);
            if (diaryDate == null) {
                continue;
            }

            ObjectNode entry = objectMapper.createObjectNode();
            entry.put("date", diaryDate.format(DATE_FORMATTER));
            entry.put("diaryId", diary.getId());
            entries.add(entry);
        }

        calendar.set("entries", entries);
        return calendar;
    }

    private ObjectNode buildTopics(List<Diary> diaries) {
        List<TopicCount> topicCounts = diaries.stream()
                .flatMap(diary -> collectTopics(diary).stream())
                .collect(Collectors.groupingBy(
                        topic -> topic,
                        LinkedHashMap::new,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> new TopicCount(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(TopicCount::count).reversed().thenComparing(TopicCount::name))
                .toList();

        ObjectNode topics = objectMapper.createObjectNode();
        if (topicCounts.isEmpty()) {
            topics.putNull("top1");
            topics.set("top2to4", objectMapper.createArrayNode());
            return topics;
        }

        long total = diaries.size();
        TopicCount top = topicCounts.get(0);

        ObjectNode top1 = objectMapper.createObjectNode();
        top1.put("name", top.name());
        top1.put("ratio", ratio(top.count(), total));
        top1.putNull("description");
        topics.set("top1", top1);

        ArrayNode top2to4 = objectMapper.createArrayNode();
        topicCounts.stream()
                .skip(1)
                .limit(3)
                .forEach(topic -> {
                    ObjectNode node = objectMapper.createObjectNode();
                    node.put("name", topic.name());
                    node.put("ratio", ratio(topic.count(), total));
                    top2to4.add(node);
                });
        topics.set("top2to4", top2to4);

        return topics;
    }

    private ArrayNode buildWeeks(YearMonth yearMonth, List<Diary> diaries) {
        ArrayNode weeks = objectMapper.createArrayNode();
        Map<Integer, List<Diary>> diariesByWeek = diaries.stream()
                .filter(diary -> resolveDiaryDate(diary) != null)
                .collect(Collectors.groupingBy(diary -> weekIndex(resolveDiaryDate(diary))));

        List<WeekRange> weekRanges = buildCalendarWeekRanges(yearMonth);
        for (WeekRange range : weekRanges) {
            int weekIndex = range.weekIndex();

            List<Diary> weekDiaries = diariesByWeek.getOrDefault(weekIndex, List.of());
            EmotionStat emotionStat = topEmotion(weekDiaries);

            ObjectNode week = objectMapper.createObjectNode();
            week.put("weekIndex", weekIndex);
            week.put("startDate", range.startDate().format(DATE_FORMATTER));
            week.put("endDate", range.endDate().format(DATE_FORMATTER));
            if (emotionStat.emotion() == null) {
                week.putNull("topEmotion");
            } else {
                week.put("topEmotion", emotionStat.emotion());
            }
            week.put("emotionRate", emotionStat.rate());
            weeks.add(week);
        }

        return weeks;
    }

    private JsonNode buildLastMonthReminder(Long userId, YearMonth yearMonth) {
        YearMonth previousMonth = yearMonth.minusMonths(1);
        LocalDate startDate = previousMonth.atDay(1);
        LocalDate endDate = previousMonth.atEndOfMonth();

        List<Diary> previousDiaries = diaryRepository
                .findByUser_IdAndDeletedAtIsNullAndDiaryDateBetweenOrderByDiaryDateAsc(
                        userId,
                        startDate,
                        endDate
                )
                .stream()
                .filter(this::isReportableDiary)
                .toList();

        if (previousDiaries.isEmpty()) {
            return objectMapper.nullNode();
        }

        Diary reminder = previousDiaries.get(ThreadLocalRandom.current().nextInt(previousDiaries.size()));
        ObjectNode node = objectMapper.createObjectNode();
        node.put("sourceYearMonth", previousMonth.format(YEAR_MONTH_FORMATTER));
        node.put("diaryId", reminder.getId());
        node.put("date", resolveDiaryDate(reminder).format(DATE_FORMATTER));
        node.put("title", reminder.getTitle());
        ArrayNode topics = objectMapper.createArrayNode();
        collectTopics(reminder).forEach(topics::add);
        node.set("topics", topics);
        node.put("emotion", reminder.getMainEmotion());
        return node;
    }

    private boolean isReportableDiary(Diary diary) {
        if (diary == null || diary.getDeletedAt() != null) {
            return false;
        }
        if (!STATUS_ENDED.equalsIgnoreCase(diary.getConversationStatus())) {
            return false;
        }
        if (includeOnlyMarked && !Boolean.TRUE.equals(diary.getIncludedInMonthlyReport())) {
            return false;
        }
        return diary.getContent() != null && !diary.getContent().isBlank();
    }

    private LocalDate resolveDiaryDate(Diary diary) {
        if (diary.getDiaryDate() != null) {
            return diary.getDiaryDate();
        }
        if (diary.getCreatedAt() != null) {
            return diary.getCreatedAt().toLocalDate();
        }
        return null;
    }

    private List<String> collectTopics(Diary diary) {
        return java.util.stream.Stream.of(diary.getTopic1(), diary.getTopic2())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(topic -> !topic.isBlank())
                .toList();
    }

    private EmotionStat topEmotion(List<Diary> diaries) {
        if (diaries.isEmpty()) {
            return new EmotionStat(null, 0);
        }

        Map<String, Long> counts = diaries.stream()
                .map(Diary::getMainEmotion)
                .filter(Objects::nonNull)
                .map(emotion -> emotion.trim().toLowerCase(Locale.ROOT))
                .filter(emotion -> !emotion.isBlank())
                .collect(Collectors.groupingBy(
                        emotion -> emotion,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        if (counts.isEmpty()) {
            return new EmotionStat(null, 0);
        }

        Map.Entry<String, Long> top = counts.entrySet()
                .stream()
                .max(Map.Entry.<String, Long>comparingByValue().thenComparing(Map.Entry::getKey))
                .orElseThrow();

        return new EmotionStat(top.getKey(), ratio(top.getValue(), diaries.size()));
    }

    private int weekIndex(LocalDate date) {
        LocalDate firstDay = YearMonth.from(date).atDay(1);
        LocalDate firstWeekStart = firstDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return (int) ((date.toEpochDay() - firstWeekStart.toEpochDay()) / 7) + 1;
    }

    private List<WeekRange> buildCalendarWeekRanges(YearMonth yearMonth) {
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();
        LocalDate currentStart = monthStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<WeekRange> ranges = new ArrayList<>();
        int weekIndex = 1;
        while (!currentStart.isAfter(monthEnd)) {
            LocalDate currentEnd = currentStart.plusDays(6);
            LocalDate displayStart = currentStart.isBefore(monthStart) ? monthStart : currentStart;
            LocalDate displayEnd = currentEnd.isAfter(monthEnd) ? monthEnd : currentEnd;
            ranges.add(new WeekRange(weekIndex++, displayStart, displayEnd));
            currentStart = currentStart.plusWeeks(1);
        }
        return ranges;
    }

    private int ratio(long count, long total) {
        if (total <= 0) {
            return 0;
        }
        return (int) Math.round((count * 100.0) / total);
    }

    private String readText(JsonNode node, String firstKey, String secondKey) {
        if (node == null || node.isNull()) {
            return null;
        }
        JsonNode first = node.get(firstKey);
        if (first == null || first.isNull()) {
            return null;
        }
        JsonNode second = first.get(secondKey);
        if (second == null || second.isNull()) {
            return null;
        }
        String value = second.asText();
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record TopicCount(String name, long count) {}

    private record EmotionStat(String emotion, int rate) {}

    private record WeekRange(int weekIndex, LocalDate startDate, LocalDate endDate) {}
}
