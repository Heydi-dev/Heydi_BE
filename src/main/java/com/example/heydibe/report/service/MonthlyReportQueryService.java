package com.example.heydibe.report.service;

import com.example.heydibe.common.api.ApiException;
import com.example.heydibe.report.domain.MonthlyReport;
import com.example.heydibe.report.dto.MonthlyReportApiDto.*;
import com.example.heydibe.report.repository.MonthlyReportRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

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
            throw new ApiException(6001, "월간 리포트 목록을 불러오지 못했습니다.");
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
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            // 통합 조회 에러코드는 너가 따로 정하면 됨 (일단 6002로 잡음)

            throw new ApiException(6002, "월간 리포트를 불러오지 못했습니다.");
        }
    }

    // ✅ topics: /reports/monthly/{yearMonth}/topics
    public TopicsResult getTopics(Long userId, String yearMonth) {
        YearMonth ym = parseYearMonthOrThrow(yearMonth);

        try {
            JsonNode root = readAnalysisRoot(userId, ym.toString());
            JsonNode arr = root.path("topics");
            if (!arr.isArray()) {
                throw new ApiException(6003, "월간 주제 데이터를 불러오지 못했습니다.");
            }

            List<JsonNode> list = new ArrayList<>();
            for (JsonNode n : arr) list.add(n);

            if (list.isEmpty()) {
                throw new ApiException(6003, "월간 주제 데이터를 불러오지 못했습니다.");
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
                throw new ApiException(6003, "월간 주제 데이터를 불러오지 못했습니다.");
            }

            return new TopicsResult(yearMonth, top1, top2to4);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(6003, "월간 주제 데이터를 불러오지 못했습니다.");
        }
    }

    // B) calendar (명세가 entries만)
    public CalendarResult getCalendar(Long userId, String yearMonth) {
        parseYearMonthOrThrow(yearMonth);
        try {
            // ✅ 여기서는 기존 DiaryRepository 연동 버전이 너 프로젝트에 이미 있었던 걸로 알고 있어.
            // 지금 답변에서는 "report 수정분"이 핵심이라서, 기존 구현을 그대로 두고
            // 응답 DTO만 CalendarResult(entries) 형태로 맞추면 됨.
            //
            // 만약 네가 diaryRepository 기반 구현을 쓰고 있으면,
            // return new CalendarResult(entries);
            //
            // 임시: 빈 배열
            return new CalendarResult(List.of());
        } catch (Exception e) {
            throw new ApiException(6007, "캘린더 데이터를 불러오지 못했습니다.");
        }
    }

    // ---------------------
    // helpers
    // ---------------------
    private YearMonth parseYearMonthOrThrow(String yearMonth) {
        try {
            return YearMonth.parse(yearMonth);
        } catch (Exception e) {
            throw new ApiException(4000, "yearMonth 형식이 올바르지 않습니다. (예: 2025-12)");
        }
    }

    private JsonNode readAnalysisRoot(Long userId, String yearMonth) {
        MonthlyReport mr = monthlyReportRepository.findByUserIdAndReportYearMonth(userId, yearMonth)
                .orElseThrow(() -> new ApiException(6001, "월간 리포트 목록을 불러오지 못했습니다."));

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