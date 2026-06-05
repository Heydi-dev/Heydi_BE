package com.example.heydibe.report.service;

import com.example.heydibe.ai.service.AiService;
import com.example.heydibe.diary.entity.Diary;
import com.example.heydibe.diary.repository.DiaryRepository;
import com.example.heydibe.report.domain.MonthlyReport;
import com.example.heydibe.report.repository.MonthlyReportRepository;
import com.example.heydibe.user.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyReportUpdateServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private MonthlyReportRepository monthlyReportRepository;

    @Mock
    private AiService aiService;

    @Test
    void updateReport_createsMonthlyReportAnalysisJson() throws Exception {
        Long userId = 1L;
        YearMonth yearMonth = YearMonth.of(2025, 1);
        MonthlyReportUpdateService service = new MonthlyReportUpdateService(
                diaryRepository,
                monthlyReportRepository,
                aiService,
                objectMapper
        );

        Diary first = createDiary(10L, userId, LocalDate.of(2025, 1, 3), "pasta was good", "joy", "pasta", "lunch");
        Diary second = createDiary(11L, userId, LocalDate.of(2025, 1, 12), "overtime was hard", "sad", "overtime", "pasta");
        Diary firstReminder = createDiary(8L, userId, LocalDate.of(2024, 12, 5), "previous month one", "calm", "rest", null);
        Diary secondReminder = createDiary(9L, userId, LocalDate.of(2024, 12, 20), "previous month two", "happy", "walk", null);

        when(diaryRepository.findByUser_IdAndDeletedAtIsNullAndDiaryDateBetweenOrderByDiaryDateAsc(
                userId,
                yearMonth.atDay(1),
                yearMonth.atEndOfMonth()
        )).thenReturn(List.of(first, second));
        when(diaryRepository.findByUser_IdAndDeletedAtIsNullAndDiaryDateBetweenOrderByDiaryDateAsc(
                userId,
                LocalDate.of(2024, 12, 1),
                LocalDate.of(2024, 12, 31)
        )).thenReturn(List.of(firstReminder, secondReminder));
        when(aiService.generateMonthlyPreferences(anyList())).thenReturn(objectMapper.readTree("""
                {
                  "like": {"keyword": "pasta", "evidence": []},
                  "dislike": {"keyword": "overtime", "evidence": []}
                }
                """));
        when(aiService.generateMonthlyActivityComment(anyList())).thenReturn("You ate pasta often this month.");
        when(aiService.generateMonthlyFeedbackComment(anyList())).thenReturn("Try to make more time for rest.");
        when(monthlyReportRepository.findByUserIdAndReportYearMonth(userId, "2025-01"))
                .thenReturn(Optional.empty());

        service.updateReport(userId, yearMonth);

        ArgumentCaptor<MonthlyReport> captor = ArgumentCaptor.forClass(MonthlyReport.class);
        verify(monthlyReportRepository).save(captor.capture());

        MonthlyReport report = captor.getValue();
        JsonNode json = report.getAnalysisJson();

        assertThat(report.getUserId()).isEqualTo(userId);
        assertThat(report.getReportYearMonth()).isEqualTo("2025-01");
        assertThat(json.path("preferences").path("like").asText()).isEqualTo("pasta");
        assertThat(json.path("preferences").path("dislike").asText()).isEqualTo("overtime");
        assertThat(json.path("preferenceDetails").path("like").path("keyword").asText()).isEqualTo("pasta");
        assertThat(json.path("summary").asText()).isEqualTo("You ate pasta often this month.");
        assertThat(json.path("monthlyInsight").asText()).isEqualTo("Try to make more time for rest.");
        assertThat(json.path("calendar").path("entries").size()).isEqualTo(2);
        assertThat(json.path("topics").path("top1").path("name").asText()).isEqualTo("pasta");
        assertThat(json.path("topics").path("top1").path("ratio").asInt()).isEqualTo(100);
        assertThat(json.path("weeks").size()).isEqualTo(5);
        assertThat(json.path("weeks").path(0).path("startDate").asText()).isEqualTo("2025-01-01");
        assertThat(json.path("weeks").path(0).path("endDate").asText()).isEqualTo("2025-01-05");
        assertThat(json.path("lastMonthReminder").path("sourceYearMonth").asText()).isEqualTo("2024-12");
        assertThat(json.path("lastMonthReminder").path("diaryId").asLong()).isIn(8L, 9L);
        assertThat(json.path("lastMonthReminder").path("date").asText()).isIn("2024-12-05", "2024-12-20");
    }

    private Diary createDiary(Long diaryId, Long userId, LocalDate date, String content, String emotion, String topic1, String topic2) {
        User user = User.builder()
                .id(userId)
                .username("tester")
                .nickname("tester")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        Diary diary = new Diary();
        diary.setId(diaryId);
        diary.setUser(user);
        diary.setTitle("Diary of " + date);
        diary.setContent(content);
        diary.setSummaryOneLine(content);
        diary.setMainEmotion(emotion);
        diary.setTopic1(topic1);
        diary.setTopic2(topic2);
        diary.setConversationStatus("ENDED");
        diary.setDiaryDate(date);
        diary.setCreatedAt(date.atTime(21, 0));
        diary.setUpdatedAt(date.atTime(21, 0));
        return diary;
    }
}
