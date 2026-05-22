package com.example.heydibe.diary.service;

import com.example.heydibe.ai.service.AiService;
import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.diary.dto.request.ConversationSessionStartRequest;
import com.example.heydibe.diary.dto.response.ConversationMessageHistoryResponse;
import com.example.heydibe.diary.dto.response.ConversationSessionEndResponse;
import com.example.heydibe.diary.dto.response.ConversationSessionStartResponse;
import com.example.heydibe.diary.entity.Diary;
import com.example.heydibe.diary.entity.DiaryConversation;
import com.example.heydibe.diary.repository.DiaryConversationRepository;
import com.example.heydibe.diary.repository.DiaryRepository;
import com.example.heydibe.report.service.MonthlyReportUpdateService;
import com.example.heydibe.user.entity.User;
import com.example.heydibe.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationSessionServiceTest {

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private DiaryConversationRepository diaryConversationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AiService aiService;

    @Mock
    private MonthlyReportUpdateService monthlyReportUpdateService;

    @InjectMocks
    private ConversationSessionService conversationSessionService;

    // ==================== 성공 케이스 ====================

    @Test
    void startSession_withTargetDate_createsActiveDiary() {
        // 성공: 유효한 targetDate로 세션 시작 시 ACTIVE 일기가 생성된다.
        Long userId = 1L;
        ConversationSessionStartRequest request = new ConversationSessionStartRequest();
        request.setTargetDate("2025-11-21");

        User user = createUser(userId);
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(diaryRepository.save(any(Diary.class))).thenAnswer(invocation -> {
            Diary diary = invocation.getArgument(0);
            diary.setId(10L);
            return diary;
        });

        ConversationSessionStartResponse response = conversationSessionService.startSession(userId, request);

        assertThat(response.getDiaryId()).isEqualTo(10L);
        assertThat(response.getDate()).isEqualTo("2025-11-21");
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        assertThat(response.getStartedAt()).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");

        ArgumentCaptor<Diary> diaryCaptor = ArgumentCaptor.forClass(Diary.class);
        verify(diaryRepository).save(diaryCaptor.capture());
        Diary savedDiary = diaryCaptor.getValue();
        assertThat(savedDiary.getDiaryDate()).isEqualTo(LocalDate.of(2025, 11, 21));
        assertThat(savedDiary.getConversationStatus()).isEqualTo("ACTIVE");
        assertThat(savedDiary.getContent()).isEmpty();
    }

    @Test
    void getMessageHistory_returnsConversationMessages() {
        // 성공: 메시지 히스토리 조회 시 role/text가 원본 그대로 반환된다.
        Long userId = 1L;
        Long diaryId = 20L;
        User user = createUser(userId);
        Diary diary = createDiary(diaryId, user, "ACTIVE");

        DiaryConversation userMessage = DiaryConversation.builder()
                .diary(diary)
                .sender("USER")
                .messageText("오늘 너무 피곤했어.")
                .createdAt(LocalDateTime.of(2025, 11, 21, 21, 3, 10))
                .build();
        DiaryConversation assistantMessage = DiaryConversation.builder()
                .diary(diary)
                .sender("AI")
                .messageText("요즘 많이 바빴나 봐요.")
                .createdAt(LocalDateTime.of(2025, 11, 21, 21, 3, 11))
                .build();

        when(diaryRepository.findByIdAndDeletedAtIsNull(diaryId)).thenReturn(Optional.of(diary));
        when(diaryConversationRepository.findPageByDiaryId(eq(diaryId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(userMessage, assistantMessage)));

        ConversationMessageHistoryResponse response =
                conversationSessionService.getMessageHistory(userId, diaryId, 0, 20);

        assertThat(response.getMessages()).hasSize(2);
        assertThat(response.getMessages().get(0).getRole()).isEqualTo("USER");
        assertThat(response.getMessages().get(0).getText()).isEqualTo("오늘 너무 피곤했어.");
        assertThat(response.getMessages().get(1).getRole()).isEqualTo("AI");
        assertThat(response.getMessages().get(1).getText()).isEqualTo("요즘 많이 바빴나 봐요.");
    }

    @Test
    void saveConversationMessage_normalizesSenderBeforeSave() {
        Long userId = 1L;
        Long diaryId = 21L;
        User user = createUser(userId);
        Diary diary = createDiary(diaryId, user, "ACTIVE");

        when(diaryRepository.findByIdAndDeletedAtIsNull(diaryId)).thenReturn(Optional.of(diary));

        conversationSessionService.saveConversationMessage(userId, diaryId, "assistant", "hello");

        ArgumentCaptor<DiaryConversation> conversationCaptor = ArgumentCaptor.forClass(DiaryConversation.class);
        verify(diaryConversationRepository).save(conversationCaptor.capture());

        DiaryConversation saved = conversationCaptor.getValue();
        assertThat(saved.getSender()).isEqualTo("AI");
        assertThat(saved.getMessageText()).isEqualTo("hello");
    }

    @Test
    void endSession_generatesDiaryAndMarksEnded() {
        // 성공: 세션 종료 시 AI 생성 결과가 저장되고 상태가 ENDED로 바뀐다.
        Long userId = 1L;
        Long diaryId = 30L;
        User user = createUser(userId);
        Diary diary = createDiary(diaryId, user, "ACTIVE");
        diary.setDiaryDate(LocalDate.of(2025, 11, 21));
        diary.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        diary.setContent("");

        DiaryConversation c1 = DiaryConversation.builder()
                .diary(diary)
                .sender("AI")
                .messageText("오늘은 무슨 일이 있었나요?")
                .createdAt(LocalDateTime.of(2025, 11, 21, 21, 3, 0))
                .build();
        DiaryConversation c2 = DiaryConversation.builder()
                .diary(diary)
                .sender("USER")
                .messageText("출근길에 비가 와서 우산을 챙겼어.")
                .createdAt(LocalDateTime.of(2025, 11, 21, 21, 3, 5))
                .build();

        String diaryContent = "오늘은 비가 와서 우산을 챙기고 바쁘게 하루를 보냈다.";
        when(diaryRepository.findByIdAndDeletedAtIsNull(diaryId)).thenReturn(Optional.of(diary));
        when(diaryConversationRepository.findAllByDiaryId(diaryId)).thenReturn(List.of(c1, c2));
        when(aiService.generateDiaryContent(anyList())).thenReturn(diaryContent);
        when(aiService.generateOneLineDiary(diaryContent)).thenReturn("비 오는 출근길에도 차분히 하루를 보냈다.");
        when(aiService.generateEmotion(diaryContent)).thenReturn("joy");
        when(aiService.generateTopics(diaryContent)).thenReturn(List.of("화해", "학교"));
        when(diaryRepository.save(any(Diary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConversationSessionEndResponse response = conversationSessionService.endSession(userId, diaryId, null);

        assertThat(response.getStatus()).isEqualTo("ENDED");
        assertThat(response.getEndedAt()).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");
        assertThat(response.getDiary().getId()).isEqualTo(diaryId);
        assertThat(response.getDiary().getContent()).isEqualTo(diaryContent);
        assertThat(response.getDiary().getOneLineDiary()).isEqualTo("비 오는 출근길에도 차분히 하루를 보냈다.");
        assertThat(response.getDiary().getEmotionCategory()).isEqualTo("joy");
        assertThat(response.getDiary().getTopic()).containsExactly("화해", "학교");

        // 응답 JSON에 emotionText 필드가 없어야 한다.
        Map<String, Object> diaryJson = new ObjectMapper().convertValue(response.getDiary(), Map.class);
        assertThat(diaryJson).doesNotContainKey("emotionText");

        assertThat(diary.getConversationStatus()).isEqualTo("ENDED");
        assertThat(diary.getMainEmotion()).isEqualTo("joy");
        assertThat(diary.getTopic1()).isEqualTo("화해");
        assertThat(diary.getTopic2()).isEqualTo("학교");
        assertThat(diary.getConversationDurationSeconds()).isGreaterThanOrEqualTo(0);

        verify(aiService).generateDiaryContent(anyList());
        verify(aiService).generateOneLineDiary(diaryContent);
        verify(aiService).generateEmotion(diaryContent);
        verify(aiService).generateTopics(diaryContent);
        verify(monthlyReportUpdateService).updateReportForDiary(diary);
    }

    // ==================== 실패 케이스 ====================

    @Test
    void startSession_withInvalidTargetDate_throwsTargetDateInvalid() {
        // 실패: 날짜 형식이 잘못되면 targetDate 에러를 반환한다.
        Long userId = 1L;
        ConversationSessionStartRequest request = new ConversationSessionStartRequest();
        request.setTargetDate("2025/11/21");

        User user = createUser(userId);
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> conversationSessionService.startSession(userId, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONVERSATION_TARGET_DATE_INVALID);

        verify(diaryRepository, never()).save(any(Diary.class));
    }

    @Test
    void startSession_withDuplicateDiaryDate_throwsDiaryAlreadyExists() {
        // 실패: 같은 날짜 일기가 이미 있으면 중복 에러를 반환한다.
        Long userId = 1L;
        ConversationSessionStartRequest request = new ConversationSessionStartRequest();
        request.setTargetDate("2025-11-21");

        User user = createUser(userId);
        Diary existingDiary = createDiary(99L, user, "ENDED");
        existingDiary.setDiaryDate(LocalDate.of(2025, 11, 21));

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(diaryRepository.findByUser_IdAndDeletedAtIsNull(userId)).thenReturn(List.of(existingDiary));

        assertThatThrownBy(() -> conversationSessionService.startSession(userId, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONVERSATION_DIARY_ALREADY_EXISTS);

        verify(diaryRepository, never()).save(any(Diary.class));
    }

    @Test
    void getMessageHistory_withInvalidPageRequest_throwsInvalidPageRequest() {
        // 실패: 페이지 파라미터가 잘못되면 INVALID_PAGE_REQUEST를 반환한다.
        assertThatThrownBy(() -> conversationSessionService.getMessageHistory(1L, 10L, -1, 20))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PAGE_REQUEST);

        verifyNoInteractions(diaryRepository, diaryConversationRepository);
    }

    @Test
    void getMessageHistory_whenDiaryNotFound_throwsDiaryNotFound() {
        // 실패: 존재하지 않는 diaryId 조회 시 DIARY_NOT_FOUND를 반환한다.
        Long userId = 1L;
        Long diaryId = 999L;
        when(diaryRepository.findByIdAndDeletedAtIsNull(diaryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationSessionService.getMessageHistory(userId, diaryId, 0, 20))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DIARY_NOT_FOUND);
    }

    @Test
    void endSession_whenDiaryAlreadyEnded_throwsConversationAlreadyEnded() {
        // 실패: 이미 종료된 세션을 다시 종료하면 CONVERSATION_ALREADY_ENDED를 반환한다.
        Long userId = 1L;
        Long diaryId = 40L;
        User user = createUser(userId);
        Diary diary = createDiary(diaryId, user, "ENDED");

        when(diaryRepository.findByIdAndDeletedAtIsNull(diaryId)).thenReturn(Optional.of(diary));

        assertThatThrownBy(() -> conversationSessionService.endSession(userId, diaryId, null))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONVERSATION_ALREADY_ENDED);

        verify(diaryRepository, never()).save(any(Diary.class));
        verifyNoInteractions(aiService);
    }

    @Test
    void endSession_whenDiaryNotFound_throwsDiaryNotFound() {
        // 실패: 존재하지 않는 diaryId 종료 요청 시 DIARY_NOT_FOUND를 반환한다.
        Long userId = 1L;
        Long diaryId = 404L;
        when(diaryRepository.findByIdAndDeletedAtIsNull(diaryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationSessionService.endSession(userId, diaryId, null))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DIARY_NOT_FOUND);

        verifyNoInteractions(aiService);
    }

    private User createUser(Long userId) {
        return User.builder()
                .id(userId)
                .username("tester")
                .nickname("tester")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    private Diary createDiary(Long diaryId, User user, String conversationStatus) {
        Diary diary = new Diary();
        diary.setId(diaryId);
        diary.setUser(user);
        diary.setContent("");
        diary.setPublic(false);
        diary.setIncludedInMonthlyReport(false);
        diary.setConversationDurationSeconds(0);
        diary.setConversationStatus(conversationStatus);
        diary.setCreatedAt(LocalDateTime.now().minusMinutes(10));
        diary.setUpdatedAt(LocalDateTime.now().minusMinutes(10));
        return diary;
    }
}
