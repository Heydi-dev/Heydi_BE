package com.example.heydibe.report.service;

import com.example.heydibe.common.api.ApiException;
import com.example.heydibe.diary.domain.Diary;
import com.example.heydibe.diary.domain.DiaryTag;
import com.example.heydibe.diary.repository.DiaryRepository;
import com.example.heydibe.diary.repository.DiaryTagRepository;
import com.example.heydibe.report.domain.MonthlyReport;
import com.example.heydibe.report.dto.MonthlyReportApiDto.*;
import com.example.heydibe.report.repository.MonthlyReportRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class MonthlyReportQueryService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final MonthlyReportRepository monthlyReportRepository;
    private final DiaryRepository diaryRepository;
    private final DiaryTagRepository diaryTagRepository;
    private final ObjectMapper objectMapper;

    public MonthlyReportQueryService(
            MonthlyReportRepository monthlyReportRepository,
            DiaryRepository diaryRepository,
            DiaryTagRepository diaryTagRepository,
            ObjectMapper objectMapper
    ) {
        this.monthlyReportRepository = monthlyReportRepository;
        this.diaryRepository = diaryRepository;
        this.diaryTagRepository = diaryTagRepository;
        this.objectMapper = objectMapper;
    }

    // ------------------------
    // A) /reports/monthly
    // ------------------------
    public AvailableMonthsResult getAvailableMonths(Long userId) {
        try {
            List<String> months = monthlyReportRepository.findAvailableMonths(userId);
            String defaultYm = monthlyReportRepository.findDefaultYearMonth(userId);
            return new AvailableMonthsResult(months, defaultYm);
        } catch (Exception e) {
            throw new ApiException(6001, "월간 리포트 목록을 불러오지 못했습니다.");
        }
    }

    // ------------------------
    // B) /calendar
    // ------------------------
    public CalendarResult getCalendar(Long userId, String yearMonth) {
        YearMonth ym = parseYearMonthOrThrow(yearMonth);

        try {
            Instant start = startOfMonth(ym);
            Instant end = startOfMonth(ym.plusMonths(1));

            List<Diary> diaries = diaryRepository.findDiariesInRange(userId, start, end);

            List<CalendarEntry> entries = diaries.stream()
                    .map(d -> new CalendarEntry(
                            DATE_FMT.format(d.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate()),
                            d.getDiaryId()
                    ))
                    .toList();

            return new CalendarResult(yearMonth, entries);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(6601, "캘린더 데이터를 불러오지 못했습니다.");
        }
    }

    // ------------------------
    // B) /reminder  (지난달 일기)
    // ------------------------
    public ReminderResult getReminder(Long userId, String baseYearMonth) {
        YearMonth base = parseYearMonthOrThrow(baseYearMonth);
        YearMonth source = base.minusMonths(1);

        Instant start = startOfMonth(source);
        Instant end = startOfMonth(source.plusMonths(1));

        Diary diary = diaryRepository.findLatestDiaryInRange(userId, start, end)
                .orElseThrow(() -> new ApiException(6701, "지난 달에 작성된 일기가 없습니다."));

        String date = DATE_FMT.format(diary.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate());

        String topic = diaryTagRepository.findFirstTopicTag(diary.getDiaryId())
                .map(DiaryTag::getTagName)
                .orElse(null);

        return new ReminderResult(
                baseYearMonth,
                source.toString(),
                diary.getDiaryId(),
                date,
                diary.getTitle(),
                topic,
                diary.getMainEmotion()
        );
    }

    // ------------------------
    // C) analysis_json 기반
    // ------------------------
    public InsightsResult getInsights(Long userId, String yearMonth) {
        YearMonth ym = parseYearMonthOrThrow(yearMonth);

        try {
            JsonNode root = readAnalysisRoot(userId, ym);
            JsonNode node = root.path("insights");
            if (node.isMissingNode() || node.isNull()) {
                throw new ApiException(6501, "인사이트 리포트를 생성하지 못했습니다.");
            }

            String summary = textOrNull(node, "summary");
            List<String> highlights = stringArrayOrEmpty(node.path("highlights"));
            List<String> improvements = stringArrayOrEmpty(node.path("improvements"));
            String encouragement = textOrNull(node, "encouragement");

            if (summary == null && highlights.isEmpty() && improvements.isEmpty() && encouragement == null) {
                throw new ApiException(6501, "인사이트 리포트를 생성하지 못했습니다.");
            }

            return new InsightsResult(yearMonth, summary, highlights, improvements, encouragement);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(6501, "인사이트 리포트를 생성하지 못했습니다.");
        }
    }

    public ActivitiesResult getActivities(Long userId, String yearMonth) {
        YearMonth ym = parseYearMonthOrThrow(yearMonth);

        try {
            JsonNode root = readAnalysisRoot(userId, ym);
            JsonNode arr = root.path("activities");
            if (!arr.isArray()) {
                throw new ApiException(6401, "활동 리포트를 불러올 수 없습니다.");
            }

            List<ActivityItem> items = streamArray(arr)
                    .map(n -> new ActivityItem(
                            n.path("name").asText(""),
                            n.path("count").asInt(0),
                            n.path("ratio").asInt(0)
                    ))
                    .filter(it -> it.name() != null && !it.name().isBlank())
                    .toList();

            if (items.isEmpty()) {
                throw new ApiException(6401, "활동 리포트를 불러올 수 없습니다.");
            }

            return new ActivitiesResult(yearMonth, items);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(6401, "활동 리포트를 불러올 수 없습니다.");
        }
    }

    public PreferencesResult getPreferences(Long userId, String yearMonth) {
        YearMonth ym = parseYearMonthOrThrow(yearMonth);

        try {
            JsonNode root = readAnalysisRoot(userId, ym);
            JsonNode node = root.path("preferences");
            if (node.isMissingNode() || node.isNull()) {
                throw new ApiException(6301, "선호/비선호 리포트를 생성할 수 없습니다.");
            }

            JsonNode likeNode = node.path("like");
            JsonNode dislikeNode = node.path("dislike");

            String likeKeyword = textOrNull(likeNode, "keyword");
            String likeDesc = textOrNull(likeNode, "description");

            String dislikeKeyword = textOrNull(dislikeNode, "keyword");
            String dislikeDesc = textOrNull(dislikeNode, "description");

            if (likeKeyword == null && dislikeKeyword == null) {
                throw new ApiException(6301, "선호/비선호 리포트를 생성할 수 없습니다.");
            }

            PreferenceBlock like = new PreferenceBlock("좋아하는 것", likeKeyword, likeDesc);
            PreferenceBlock dislike = new PreferenceBlock("싫어하는 것", dislikeKeyword, dislikeDesc);

            return new PreferencesResult(yearMonth, like, dislike);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(6301, "선호/비선호 리포트를 생성할 수 없습니다.");
        }
    }

    public TopicsResult getTopics(Long userId, String yearMonth) {
        YearMonth ym = parseYearMonthOrThrow(yearMonth);

        try {
            JsonNode root = readAnalysisRoot(userId, ym);
            JsonNode arr = root.path("topics");
            if (!arr.isArray()) {
                throw new ApiException(6201, "해당 월의 주제 리포트를 불러올 수 없습니다.");
            }

            List<TopicItem> items = streamArray(arr)
                    .map(n -> new TopicItem(
                            n.path("name").asText(""),
                            n.path("count").asInt(0),
                            n.path("weight").asDouble(0.0)
                    ))
                    .filter(it -> it.name() != null && !it.name().isBlank())
                    .toList();

            if (items.isEmpty()) {
                throw new ApiException(6201, "해당 월의 주제 리포트를 불러올 수 없습니다.");
            }

            return new TopicsResult(yearMonth, items);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(6201, "해당 월의 주제 리포트를 불러올 수 없습니다.");
        }
    }

    public EmotionsResult getEmotions(Long userId, String yearMonth) {
        YearMonth ym = parseYearMonthOrThrow(yearMonth);

        try {
            JsonNode root = readAnalysisRoot(userId, ym);
            JsonNode arr = root.path("emotions");
            if (!arr.isArray()) {
                throw new ApiException(6101, "해당 월의 감정 리포트가 존재하지 않습니다.");
            }

            List<WeekEmotion> weeks = streamArray(arr)
                    .map(n -> new WeekEmotion(
                            n.path("weekIndex").asInt(0),
                            n.path("startDate").asText(null),
                            n.path("endDate").asText(null),
                            n.path("topEmotion").asText(null),
                            n.path("emotionRate").asInt(0)
                    ))
                    .filter(w -> w.weekIndex() > 0 && w.startDate() != null && w.endDate() != null)
                    .toList();

            if (weeks.isEmpty()) {
                throw new ApiException(6101, "해당 월의 감정 리포트가 존재하지 않습니다.");
            }

            return new EmotionsResult(yearMonth, weeks);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(6101, "해당 월의 감정 리포트가 존재하지 않습니다.");
        }
    }

    // ------------------------
    // 내부 헬퍼
    // ------------------------
    private YearMonth parseYearMonthOrThrow(String yearMonth) {
        try {
            return YearMonth.parse(yearMonth);
        } catch (Exception e) {
            throw new ApiException(4000, "yearMonth 형식이 올바르지 않습니다. (예: 2025-04)");
        }
    }

    private Instant startOfMonth(YearMonth ym) {
        LocalDateTime ldt = ym.atDay(1).atStartOfDay();
        return ldt.atZone(ZoneId.systemDefault()).toInstant();
    }

    private JsonNode readAnalysisRoot(Long userId, YearMonth ym) throws JsonProcessingException {
        String yearMonth = ym.toString();

        MonthlyReport mr = monthlyReportRepository.findByUserIdAndReportYearMonth(userId, yearMonth)
                .orElseThrow(() -> new ApiException(6001, "월간 리포트 목록을 불러오지 못했습니다."));

        String json = mr.getAnalysisJson();
        if (json == null || json.isBlank()) {
            return objectMapper.createObjectNode();
        }
        return objectMapper.readTree(json);
    }

    private String textOrNull(JsonNode node, String field) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        JsonNode v = node.path(field);
        if (v.isMissingNode() || v.isNull()) return null;
        String s = v.asText();
        return (s == null || s.isBlank()) ? null : s;
    }

    private List<String> stringArrayOrEmpty(JsonNode arr) {
        if (arr == null || !arr.isArray()) return List.of();
        return streamArray(arr)
                .map(JsonNode::asText)
                .filter(s -> s != null && !s.isBlank())
                .toList();
    }

    private Stream<JsonNode> streamArray(JsonNode arr) {
        Iterator<JsonNode> it = arr.elements();
        Spliterator<JsonNode> sp = Spliterators.spliteratorUnknownSize(it, 0);
        return StreamSupport.stream(sp, false);
    }
}
