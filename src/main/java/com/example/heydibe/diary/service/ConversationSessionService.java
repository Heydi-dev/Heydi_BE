package com.example.heydibe.diary.service;

import com.example.heydibe.ai.service.AiService;
import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.diary.dto.request.ConversationSessionEndRequest;
import com.example.heydibe.diary.dto.request.ConversationSessionStartRequest;
import com.example.heydibe.diary.dto.response.ConversationMessageHistoryResponse;
import com.example.heydibe.diary.dto.response.ConversationSessionEndResponse;
import com.example.heydibe.diary.dto.response.ConversationSessionStartResponse;
import com.example.heydibe.diary.dto.response.DiaryConversationResponse;
import com.example.heydibe.diary.entity.Diary;
import com.example.heydibe.diary.entity.DiaryConversation;
import com.example.heydibe.diary.repository.DiaryConversationRepository;
import com.example.heydibe.diary.repository.DiaryRepository;
import com.example.heydibe.user.entity.User;
import com.example.heydibe.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ConversationSessionService {
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_ENDED = "ENDED";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static final Set<String> ALLOWED_EMOTIONS = Set.of(
            "happy", "joy", "neutral", "sad", "annoyed", "angry"
    );

    private final DiaryRepository diaryRepository;
    private final DiaryConversationRepository diaryConversationRepository;
    private final UserRepository userRepository;
    private final AiService aiService;

    @Transactional
    public ConversationSessionStartResponse startSession(Long userId, ConversationSessionStartRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTHENTICATION_EXPIRED));

        LocalDate targetDate = parseTargetDate(request);
        validateDuplicateDiary(userId, targetDate);

        LocalDateTime now = LocalDateTime.now();

        Diary diary = new Diary();
        diary.setUser(user);
        diary.setTitle(null);
        diary.setContent("");
        diary.setSummaryOneLine(null);
        diary.setMainEmotion(null);
        diary.setTopic1(null);
        diary.setTopic2(null);
        diary.setEmotionScore(0);
        diary.setPublic(false);
        diary.setIncludedInMonthlyReport(false);
        diary.setConversationDurationSeconds(0);
        diary.setDiaryDate(targetDate);
        diary.setConversationStatus(STATUS_ACTIVE);
        diary.setCreatedAt(now);
        diary.setUpdatedAt(now);
        diary.setDeletedAt(null);

        Diary saved = diaryRepository.save(diary);
        return new ConversationSessionStartResponse(
                saved.getId(),
                targetDate.format(DATE_FORMAT),
                now.format(DATETIME_FORMAT),
                STATUS_ACTIVE
        );
    }

    public ConversationMessageHistoryResponse getMessageHistory(Long userId, Long diaryId, Integer page, Integer size) {
        if (page == null || size == null || page < 0 || size <= 0) {
            throw new CustomException(ErrorCode.INVALID_PAGE_REQUEST);
        }

        Diary diary = getOwnedDiary(userId, diaryId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<DiaryConversation> conversationPage = diaryConversationRepository.findPageByDiaryId(diary.getId(), pageable);

        List<DiaryConversationResponse.MessageResponse> messages = conversationPage.stream()
                .map(DiaryConversationResponse.MessageResponse::from)
                .toList();
        return new ConversationMessageHistoryResponse(messages);
    }

    @Transactional
    public ConversationSessionEndResponse endSession(Long userId, Long diaryId, ConversationSessionEndRequest request) {
        Diary diary = getOwnedDiary(userId, diaryId);

        if (STATUS_ENDED.equalsIgnoreCase(diary.getConversationStatus())) {
            throw new CustomException(ErrorCode.CONVERSATION_ALREADY_ENDED);
        }

        boolean generateDiary = request == null || request.getGenerateDiary() == null || request.getGenerateDiary();
        if (generateDiary) {
            applyGeneratedDiary(diary);
        }

        LocalDateTime endedAt = LocalDateTime.now();
        LocalDateTime startedAt = diary.getCreatedAt();

        diary.setConversationStatus(STATUS_ENDED);
        if (startedAt != null) {
            long seconds = Math.max(0, Duration.between(startedAt, endedAt).getSeconds());
            diary.setConversationDurationSeconds((int) seconds);
        }
        diary.setUpdatedAt(endedAt);
        Diary saved = diaryRepository.save(diary);

        return new ConversationSessionEndResponse(
                STATUS_ENDED,
                endedAt.format(DATETIME_FORMAT),
                buildDiarySummary(saved)
        );
    }

    @Transactional
    public void saveConversationMessage(Long userId, Long diaryId, String sender, String message) {
        Diary diary = getOwnedDiary(userId, diaryId);
        if (STATUS_ENDED.equalsIgnoreCase(diary.getConversationStatus())) {
            throw new CustomException(ErrorCode.CONVERSATION_ALREADY_ENDED);
        }

        DiaryConversation conversation = DiaryConversation.builder()
                .diary(diary)
                .sender(sender)
                .messageText(message)
                .createdAt(LocalDateTime.now())
                .build();
        diaryConversationRepository.save(conversation);
    }

    public Diary getOwnedActiveSession(Long userId, Long diaryId) {
        Diary diary = getOwnedDiary(userId, diaryId);
        if (STATUS_ENDED.equalsIgnoreCase(diary.getConversationStatus())) {
            throw new CustomException(ErrorCode.CONVERSATION_ALREADY_ENDED);
        }
        return diary;
    }

    private void validateDuplicateDiary(Long userId, LocalDate targetDate) {
        List<Diary> diaries = diaryRepository.findByUser_IdAndDeletedAtIsNull(userId);
        boolean duplicated = diaries.stream()
                .map(this::resolveDiaryDate)
                .anyMatch(targetDate::equals);

        if (duplicated) {
            throw new CustomException(ErrorCode.CONVERSATION_DIARY_ALREADY_EXISTS);
        }
    }

    private LocalDate parseTargetDate(ConversationSessionStartRequest request) {
        if (request == null || request.getTargetDate() == null || request.getTargetDate().isBlank()) {
            return LocalDate.now();
        }

        try {
            return LocalDate.parse(request.getTargetDate(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.CONVERSATION_TARGET_DATE_INVALID);
        }
    }

    private Diary getOwnedDiary(Long userId, Long diaryId) {
        Diary diary = diaryRepository.findByIdAndDeletedAtIsNull(diaryId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));

        if (!diary.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.DIARY_NOT_FOUND);
        }

        return diary;
    }

    private LocalDate resolveDiaryDate(Diary diary) {
        if (diary.getDiaryDate() != null) {
            return diary.getDiaryDate();
        }
        if (diary.getCreatedAt() != null) {
            return diary.getCreatedAt().toLocalDate();
        }
        return LocalDate.MIN;
    }

    private void applyGeneratedDiary(Diary diary) {
        List<Map<String, String>> turns = collectConversationTurns(diary.getId());

        String generatedContent = aiService.generateDiaryContent(turns);
        String content = pickNonBlank(generatedContent, diary.getContent(), buildFallbackDiary(turns));
        diary.setContent(content);

        String generatedOneLine = aiService.generateOneLineDiary(content);
        String oneLine = pickNonBlank(generatedOneLine, diary.getSummaryOneLine(), content);
        diary.setSummaryOneLine(oneLine);

        String generatedEmotion = aiService.generateEmotion(content);
        String normalizedEmotion = normalizeEmotion(generatedEmotion);
        diary.setMainEmotion(normalizedEmotion != null ? normalizedEmotion : "neutral");

        List<String> generatedTopics = aiService.generateTopics(content);
        List<String> topics = normalizeTopics(generatedTopics);
        diary.setTopic1(topics.isEmpty() ? null : topics.get(0));
        diary.setTopic2(topics.size() > 1 ? topics.get(1) : null);

        if (diary.getDiaryDate() == null) {
            diary.setDiaryDate(resolveDiaryDate(diary));
        }

        LocalDate diaryDate = diary.getDiaryDate();
        if (diaryDate != null && (diary.getTitle() == null || diary.getTitle().isBlank())) {
            diary.setTitle("Diary of " + diaryDate);
        }
    }

    private List<Map<String, String>> collectConversationTurns(Long diaryId) {
        List<DiaryConversation> conversations = diaryConversationRepository.findAllByDiaryId(diaryId);
        List<DiaryConversation> sorted = conversations.stream()
                .sorted(Comparator.comparing(DiaryConversation::getCreatedAt))
                .toList();

        List<Map<String, String>> turns = new ArrayList<>();
        for (DiaryConversation msg : sorted) {
            String text = msg.getMessageText() == null ? "" : msg.getMessageText().trim();
            if (text.isEmpty()) {
                continue;
            }

            turns.add(Map.of(
                    "role", normalizeRole(msg.getSender()),
                    "text", text
            ));
        }

        if (turns.isEmpty()) {
            turns.add(Map.of(
                    "role", "user",
                    "text", "I want to organize my day."
            ));
        }

        return turns;
    }

    private String normalizeRole(String sender) {
        if (sender == null || sender.isBlank()) {
            return "user";
        }

        String value = sender.trim().toLowerCase(Locale.ROOT);
        if ("assistant".equals(value) || "ai".equals(value)) {
            return "assistant";
        }
        return "user";
    }

    private String buildFallbackDiary(List<Map<String, String>> turns) {
        return turns.stream()
                .map(turn -> turn.get("role") + ": " + turn.get("text"))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("I reflected on my day.");
    }

    private String normalizeEmotion(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        return ALLOWED_EMOTIONS.contains(normalized) ? normalized : null;
    }

    private List<String> normalizeTopics(List<String> rawTopics) {
        List<String> normalized = new ArrayList<>();
        if (rawTopics == null) {
            return normalized;
        }

        for (String topic : rawTopics) {
            if (topic == null) {
                continue;
            }
            String trimmed = topic.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            normalized.add(trimmed);
            if (normalized.size() >= 2) {
                break;
            }
        }
        return normalized;
    }

    private String pickNonBlank(String first, String second, String fallback) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return fallback;
    }

    private ConversationSessionEndResponse.DiarySummary buildDiarySummary(Diary diary) {
        String emotionCategory = diary.getMainEmotion() == null || diary.getMainEmotion().isBlank()
                ? "neutral"
                : diary.getMainEmotion();

        List<String> topics = new ArrayList<>();
        if (diary.getTopic1() != null && !diary.getTopic1().isBlank()) {
            topics.add(diary.getTopic1());
        }
        if (diary.getTopic2() != null && !diary.getTopic2().isBlank()) {
            topics.add(diary.getTopic2());
        }

        String content = diary.getContent() == null || diary.getContent().isBlank()
                ? "I reflected on my day."
                : diary.getContent();
        String oneLine = diary.getSummaryOneLine() == null || diary.getSummaryOneLine().isBlank()
                ? content
                : diary.getSummaryOneLine();

        LocalDate date = resolveDiaryDate(diary);
        String title = diary.getTitle();
        if ((title == null || title.isBlank()) && date != null && !LocalDate.MIN.equals(date)) {
            title = "Diary of " + date;
        }

        return new ConversationSessionEndResponse.DiarySummary(
                diary.getId(),
                date == null || LocalDate.MIN.equals(date) ? null : date.format(DATE_FORMAT),
                title,
                buildEmotionText(emotionCategory),
                emotionCategory,
                topics,
                oneLine,
                content
        );
    }

    private String buildEmotionText(String emotionCategory) {
        return switch (emotionCategory) {
            case "happy" -> "Today felt bright and comfortable.";
            case "joy" -> "Today had many joyful moments.";
            case "sad" -> "Today felt heavy and quiet.";
            case "annoyed" -> "Today included some irritating moments.";
            case "angry" -> "Today had moments of anger.";
            default -> "Today was mostly neutral.";
        };
    }
}
