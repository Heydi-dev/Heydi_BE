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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MonthlyReportQueryService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final MonthlyReportRepository monthlyReportRepository;
    private final DiaryRepository diaryRepository;
    private final DiaryTagRepository diaryTagRepository;
    private final ObjectMapper objectMapper;

    public MonthlyReportQueryService(MonthlyReportRepository monthlyReportRepository,
                                     DiaryRepository diaryRepository,
                                     DiaryTagRepository diaryTagRepository,
                                     ObjectMapper objectMapper) {
        this.monthlyReportRepository = monthlyReportRepository;
        this.diaryRepository = diaryRepository;
        this.diaryTagRepository = diaryTagRepository;
        this.objectMapper = objectMapper;
    }

    // ------------------------
    // 1) /reports/monthly
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
    // 2) /topics
    // ------------------------
    public TopicsResult getTopics(Long userId, String yearMonth) {
        YearMonth ym = parseYearMonthOrThrow(yearMonth, 6003, "월간 주제 데이터를 불러오지 못했습니다.");

        try {
            JsonNode root = readAnalysisRoot(userId, ym);
            JsonNode topicsArr = root.path("topics");
            if (!topicsArr.isArray() || topicsArr.isEmpty()) {
                throw new ApiException(6003, "월간 주제 데이터를 불러오지 못했습니다.");
            }

            // topics: [{name,count,weight,description?}, ...]
            List<JsonNode> nodes = new ArrayList<>();
            topicsArr.forEach(nodes::add);

            // 정렬: count desc 우선, 없으면 weight desc
            nodes.sort((a, b) -> {
                int ac = a.path("count").asInt(-1);
                int bc = b.path("count").asInt(-1);
                if (ac != -1 && bc != -1) return Integer.compare(bc, ac);
                double aw = a.path("weight").asDouble(0.0);
                double bw = b.path("weight").asDouble(0.0);
                return Double.compare(bw, aw);
            });

            JsonNode top1Node = nodes.get(0);
            String top1Name = top1Node.path("name").asText(null);
            if (top1Name == null || top1Name.isBlank()) {
                throw new ApiException(6003, "월간 주제 데이터를 불러오지 못했습니다.");
            }

            int ratio = computeRatioPercent(nodes, top1Node);
            String desc = textOrEmpty(top1Node, "description");

            TopicsResult.Top1 top1 = new TopicsResult.Top1(top1Name, ratio, desc);

            List<String> top2to4 = nodes.stream()
                    .skip(1)
                    .limit(3)
                    .map(n -> n.path("name").asText(""))
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toList());

            return new TopicsResult(ym.toString(), top1, top2to4);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(6003, "월간 주제 데이터를 불러오지 못했습니다.");
        }
    }

    // ------------------------
    // 3) /preferences
    // ------------------------
    public PreferencesResult getPreferences(Long userId, String yearMonth) {
        YearMonth ym = parseYearMonthOrThrow(yearMonth, 6004, "좋아하는 것/싫어하는 것 리포트를 불러오지 못했습니다.");

        try {
            JsonNode root = readAnalysisRoot(userId, ym);
            JsonNode pref = root.path("preferences");
            if (pref.isMissingNode() || pref.isNull()) {
                throw new ApiException(6004, "좋아하는 것/싫어하는 것 리포트를 불러오지 못했습니다.");
            }

            // 저장 형태가 둘 중 하나일 수 있어서 둘 다 지원:
            // A) preferences.like.keyword / preferences.dislike.keyword
            // B) preferences.like / preferences.dislike (string)
            String like = extractPreference(pref.path("like"));
            String dislike = extractPreference(pref.path("dislike"));

            if ((like == null || like.isBlank()) && (dislike == null || dislike.isBlank())) {
                throw new ApiException(6004, "좋아하는 것/싫어하는 것 리포트를 불러오지 못했습니다.");
            }

            return new PreferencesResult(ym.toString(), nullIfBlank(like), nullIfBlank(dislike));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(6004, "좋아하는 것/싫어하는 것 리포트를 불러오지 못했습니다.");
        }
    }

    // ------------------------
    // 4) /activities (summary 텍스트)
    // ------------------------
    public ActivitiesResult getActivities(Long userId, String yearMonth) {
        YearMonth ym = parseYearMonthOrThrow(yearMonth, 6005, "월간 활동 데이터를 불러오지 못했습니다.");

        try {
            JsonNode root = readAnalysisRoot(userId, ym);

            // 우선순위:
            // A) root.activitiesSummary (string)
            // B) root.activities.summary (string)
            // C) root.activities (array) -> top activity 기반 간단 문장 생성
            String summary = nullIfBlank(root.path("activitiesSummary").asText(null));
            if (summary == null) {
                JsonNode activitiesNode = root.path("activities");
                if (activitiesNode.isObject()) {
                    summary = nullIfBlank(activitiesNode.path("summary").asText(null));
                } else if (activitiesNode.isArray() && activitiesNode.size() > 0) {
                    JsonNode first = activitiesNode.get(0);
                    String name = first.path("name").asText("");
                    int count = first.path("count").asInt(0);
                    if (!name.isBlank()) {
                        summary = "이번 달에는 " + name + " 활동을 많이 했어요. (총 " + count + "회)";
                    }
                }
            }

            if (summary == null) {
                throw new ApiException(6005, "월간 활동 데이터를 불러오지 못했습니다.");
            }

            return new ActivitiesResult(ym.toString(), summary);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(6005, "월간 활동 데이터를 불러오지 못했습니다.");
        }
    }

    // ------------------------
    // 5) /insights (insight 텍스트)
    // ------------------------
    public InsightsResult getInsights(Long userId, String yearMonth) {
        YearMonth ym = parseYearMonthOrThrow(yearMonth, 6006, "월간 인사이트를 불러오지 못했습니다.");

        try {
            JsonNode root = readAnalysisRoot(userId, ym);

            // 우선순위:
            // A) root.insight (string)
            // B) root.insights.insight (string)
            // C) root.insights.summary (string)
            String insight = nullIfBlank(root.path("insight").asText(null));
            if (insight == null) {
                JsonNode insightsNode = root.path("insights");
                if (insightsNode.isObject()) {
                    insight = nullIfBlank(insightsNode.path("insight").asText(null));
                    if (insight == null) {
                        insight = nullIfBlank(insightsNode.path("summary").asText(null));
                    }
                }
            }

            if (insight == null) {
                throw new ApiException(6006, "월간 인사이트를 불러오지 못했습니다.");
            }

            return new InsightsResult(ym.toString(), insight);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(6006, "월간 인사이트를 불러오지 못했습니다.");
        }
    }

    // ------------------------
    // 6) /calendar
    // ------------------------
    public CalendarResult getCalendar(Long userId, String yearMonth) {
        YearMonth ym = parseYearMonthOrThrow(yearMonth, 6007, "캘린더 데이터를 불러오지 못했습니다.");

        try {
            Instant start = startOfMonth(ym);
            Instant end = startOfMonth(ym.plusMonths(1));

            List<Diary> diaries = diaryRepository.findDiariesInRange(userId, start, end);

            List<CalendarResult.CalendarEntry> entries = diaries.stream()
                    .map(d -> new CalendarResult.CalendarEntry(
                            DATE_FMT.format(d.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate()),
                            d.getDiaryId()
                    ))
                    .toList();

            return new CalendarResult(entries);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(6007, "캘린더 데이터를 불러오지 못했습니다.");
        }
    }

    // ------------------------
    // 7) /reminder
    // ------------------------
    public ReminderResult getReminder(Long userId, String baseYearMonth) {
        YearMonth base = parseYearMonthOrThrow(baseYearMonth, 6008, "리마인더 데이터를 불러오지 못했습니다.");
        YearMonth source = base.minusMonths(1);

        try {
            Instant start = startOfMonth(source);
            Instant end = startOfMonth(source.plusMonths(1));

            Diary diary = diaryRepository.findLatestDiaryInRange(userId, start, end)
                    .orElseThrow(() -> new ApiException(6008, "리마인더 데이터를 불러오지 못했습니다."));

            String date = DATE_FMT.format(diary.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate());

            String topic = diaryTagRepository.findFirstTopicTag(diary.getDiaryId())
                    .map(DiaryTag::getTagName)
                    .orElse(null);

            return new ReminderResult(
                    base.toString(),
                    source.toString(),
                    diary.getDiaryId(),
                    date,
                    diary.getTitle(),
                    topic,
                    diary.getMainEmotion()
            );
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(6008, "리마인더 데이터를 불러오지 못했습니다.");
        }
    }

    // ------------------------
    // 내부 헬퍼
    // ------------------------
    private YearMonth parseYearMonthOrThrow(String yearMonth, int code, String msg) {
        try {
            return YearMonth.parse(yearMonth);
        } catch (Exception e) {
            throw new ApiException(code, msg);
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

    private String extractPreference(JsonNode likeOrDislikeNode) {
        if (likeOrDislikeNode == null || likeOrDislikeNode.isMissingNode() || likeOrDislikeNode.isNull()) {
            return null;
        }
        if (likeOrDislikeNode.isTextual()) {
            return nullIfBlank(likeOrDislikeNode.asText(null));
        }
        // object 형태면 keyword 우선
        String keyword = nullIfBlank(likeOrDislikeNode.path("keyword").asText(null));
        if (keyword != null) return keyword;
        // fallback
        return nullIfBlank(likeOrDislikeNode.path("description").asText(null));
    }

    private int computeRatioPercent(List<JsonNode> all, JsonNode top1) {
        // count 기반 비율이 있으면 그걸로
        boolean hasCount = all.stream().anyMatch(n -> n.path("count").isNumber());
        if (hasCount) {
            int total = all.stream().mapToInt(n -> n.path("count").asInt(0)).sum();
            int c1 = top1.path("count").asInt(0);
            if (total <= 0) return 0;
            return (int) Math.round((c1 * 100.0) / total);
        }

        // weight 기반
        double totalW = all.stream().mapToDouble(n -> n.path("weight").asDouble(0.0)).sum();
        double w1 = top1.path("weight").asDouble(0.0);
        if (totalW <= 0.0) return 0;
        return (int) Math.round((w1 * 100.0) / totalW);
    }

    private String textOrEmpty(JsonNode node, String field) {
        if (node == null) return "";
        JsonNode v = node.path(field);
        if (v.isMissingNode() || v.isNull()) return "";
        String s = v.asText("");
        return s == null ? "" : s;
    }

    private String nullIfBlank(String s) {
        if (s == null) return null;
        return s.isBlank() ? null : s;
    }
}
