package com.example.heydibe.diary.websocket;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.diary.service.ConversationSessionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConversationWebSocketHandler extends AbstractWebSocketHandler {
    private static final String TYPE_INPUT_TRANSCRIPT = "INPUT_TRANSCRIPT";
    private static final String TYPE_OUTPUT_TRANSCRIPT = "OUTPUT_TRANSCRIPT";
    private static final String TYPE_AUDIO_CHUNK = "AUDIO_CHUNK";
    private static final String TYPE_TURN_COMPLETE = "TURN_COMPLETE";
    private static final String TYPE_INTERRUPT = "INTERRUPT";
    private static final String TYPE_ERROR = "ERROR";

    private final ObjectMapper objectMapper;
    private final ConversationSessionService conversationSessionService;
    private final AiRealtimeBridgeClient aiRealtimeBridgeClient;

    private final Map<String, WsContext> contexts = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Object userIdObj = session.getAttributes().get("userId");
        Object diaryIdObj = session.getAttributes().get("diaryId");
        if (!(userIdObj instanceof Long userId) || !(diaryIdObj instanceof Long diaryId)) {
            sendError(session, ErrorCode.AUTHENTICATION_EXPIRED);
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        try {
            conversationSessionService.getOwnedActiveSession(userId, diaryId);
            WsContext context = new WsContext(session.getId(), session, userId, diaryId);
            contexts.put(session.getId(), context);
            connectAiBridge(context);
        } catch (CustomException e) {
            sendError(session, e.getErrorCode());
            session.close(CloseStatus.POLICY_VIOLATION);
        } catch (Exception e) {
            log.warn("Failed to establish websocket bridge", e);
            sendError(session, ErrorCode.SERVER_ERROR);
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        WsContext context = contexts.get(session.getId());
        if (context == null) {
            sendError(session, ErrorCode.AUTHENTICATION_EXPIRED);
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(message.getPayload());
        } catch (Exception e) {
            sendError(session, ErrorCode.WS_UNSUPPORTED_MESSAGE_TYPE);
            return;
        }

        String type = root.path("type").asText();
        JsonNode data = root.get("data");

        try {
            switch (type) {
                case TYPE_INTERRUPT -> {
                    context.resetAssistantTranscript();
                    sendEnvelope(session, TYPE_INTERRUPT, true);
                }
                case TYPE_TURN_COMPLETE -> {
                    if (asBoolean(data)) {
                        completeTurn(context, true);
                    }
                    sendEnvelope(session, TYPE_TURN_COMPLETE, true);
                }
                default -> sendError(session, ErrorCode.WS_UNSUPPORTED_MESSAGE_TYPE);
            }
        } catch (CustomException e) {
            sendError(session, e.getErrorCode());
            if (e.getErrorCode() == ErrorCode.AUTHENTICATION_EXPIRED
                    || e.getErrorCode() == ErrorCode.DIARY_NOT_FOUND
                    || e.getErrorCode() == ErrorCode.CONVERSATION_ALREADY_ENDED) {
                closeContext(context, CloseStatus.POLICY_VIOLATION, false);
            }
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        WsContext context = contexts.get(session.getId());
        if (context == null) {
            sendError(session, ErrorCode.AUTHENTICATION_EXPIRED);
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        AiRealtimeBridgeClient.AiBridgeSession aiSession = context.aiSession();
        if (aiSession == null) {
            sendError(session, ErrorCode.SERVER_ERROR);
            closeContext(context, CloseStatus.SERVER_ERROR, false);
            return;
        }

        ByteBuffer payload = message.getPayload();
        byte[] bytes = new byte[payload.remaining()];
        payload.get(bytes);
        aiSession.sendBinary(bytes);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        WsContext context = contexts.remove(session.getId());
        if (context == null) {
            return;
        }
        closeAiSessionQuietly(context);
    }

    private void connectAiBridge(WsContext context) throws Exception {
        AiRealtimeBridgeClient.AiBridgeSession aiSession = aiRealtimeBridgeClient.connect(
                context.userId(),
                new AiRealtimeBridgeClient.AiBridgeListener() {
                    @Override
                    public void onText(String payload) {
                        handleAiText(context, payload);
                    }

                    @Override
                    public void onBinary(byte[] payload) {
                        handleAiBinary(context, payload);
                    }

                    @Override
                    public void onClosed(int statusCode, String reason) {
                        handleAiClosed(context, statusCode, reason);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        handleAiError(context, throwable);
                    }
                }
        );
        context.setAiSession(aiSession);
    }

    private void handleAiText(WsContext context, String payload) {
        if (context.isClosing()) {
            return;
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(payload);
        } catch (Exception e) {
            log.warn("AI text payload parse failed: {}", payload, e);
            closeContext(context, CloseStatus.SERVER_ERROR, true);
            return;
        }

        String type = root.path("type").asText("").trim().toLowerCase(Locale.ROOT);
        switch (type) {
            case "input", "input_transcript" -> handleAiTranscript(context, TYPE_INPUT_TRANSCRIPT, root);
            case "output", "output_transcript" -> handleAiTranscript(context, TYPE_OUTPUT_TRANSCRIPT, root);
            case "turn_complete" -> {
                if (extractCompletionSignal(root)) {
                    completeTurn(context, true);
                    sendEnvelopeSafely(context, TYPE_TURN_COMPLETE, true);
                }
            }
            case "interrupt" -> {
                context.resetAssistantTranscript();
                sendEnvelopeSafely(context, TYPE_INTERRUPT, true);
            }
            default -> log.debug("Ignore unsupported AI event type: {}", type);
        }
    }

    private void handleAiTranscript(WsContext context, String eventType, JsonNode root) {
        String transcript = extractTranscript(root);
        if (transcript != null && !transcript.isBlank()) {
            if (TYPE_INPUT_TRANSCRIPT.equals(eventType)) {
                context.appendUserTranscript(transcript);
            } else {
                context.appendAssistantTranscript(transcript);
            }
            sendEnvelopeSafely(context, eventType, transcript.trim());
        }

        if (readBoolean(root, "finished")) {
            if (TYPE_INPUT_TRANSCRIPT.equals(eventType)) {
                context.markInputFinished();
            } else {
                context.markOutputFinished();
            }
        }

        if (readBoolean(root, "turn_complete")) {
            completeTurn(context, true);
            sendEnvelopeSafely(context, TYPE_TURN_COMPLETE, true);
            return;
        }

        if (context.isTurnReady()) {
            completeTurn(context, false);
            sendEnvelopeSafely(context, TYPE_TURN_COMPLETE, true);
        }
    }

    private void handleAiBinary(WsContext context, byte[] payload) {
        if (context.isClosing()) {
            return;
        }
        String base64 = Base64.getEncoder().encodeToString(payload);
        sendEnvelopeSafely(context, TYPE_AUDIO_CHUNK, base64);
    }

    private void handleAiClosed(WsContext context, int statusCode, String reason) {
        if (context.isClosing()) {
            return;
        }
        log.info("AI websocket closed. statusCode={}, reason={}", statusCode, reason);
        closeContext(context, CloseStatus.SERVER_ERROR, true);
    }

    private void handleAiError(WsContext context, Throwable throwable) {
        if (context.isClosing()) {
            return;
        }
        log.warn("AI websocket error", throwable);
        closeContext(context, CloseStatus.SERVER_ERROR, true);
    }

    private void completeTurn(WsContext context, boolean forced) {
        TurnSnapshot snapshot = context.consumeTurnSnapshot(forced);
        if (snapshot == null) {
            return;
        }

        if (!snapshot.userText().isEmpty()) {
            conversationSessionService.saveConversationMessage(
                    context.userId(), context.diaryId(), "user", snapshot.userText()
            );
        }
        if (!snapshot.assistantText().isEmpty()) {
            conversationSessionService.saveConversationMessage(
                    context.userId(), context.diaryId(), "assistant", snapshot.assistantText()
            );
        }
    }

    private boolean asBoolean(JsonNode data) {
        if (data == null) {
            return false;
        }
        if (data.isBoolean()) {
            return data.booleanValue();
        }
        return "true".equalsIgnoreCase(data.asText());
    }

    private boolean extractCompletionSignal(JsonNode root) {
        if (readBoolean(root, "turn_complete")) {
            return true;
        }

        JsonNode data = root.get("data");
        return data != null && asBoolean(data);
    }

    private String extractTranscript(JsonNode root) {
        String transcription = trimToNull(root.path("transcription").asText(null));
        if (transcription != null) {
            return transcription;
        }

        JsonNode data = root.get("data");
        if (data != null && data.isTextual()) {
            return trimToNull(data.asText());
        }
        return null;
    }

    private boolean readBoolean(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        if (node == null || node.isNull()) {
            return false;
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        return "true".equalsIgnoreCase(node.asText());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void sendEnvelope(WebSocketSession session, String type, Object data) throws Exception {
        WsEnvelope envelope = new WsEnvelope(type, data);
        String json = objectMapper.writeValueAsString(envelope);
        session.sendMessage(new TextMessage(json.getBytes(StandardCharsets.UTF_8)));
    }

    private void sendError(WebSocketSession session, ErrorCode errorCode) throws Exception {
        ApiResponse<Void> data = ApiResponse.fail(errorCode.getCode(), errorCode.getMessage());
        sendEnvelope(session, TYPE_ERROR, data);
    }

    private void sendEnvelopeSafely(WsContext context, String type, Object data) {
        if (context.isClosing()) {
            return;
        }
        try {
            sendEnvelope(context.frontendSession(), type, data);
        } catch (Exception e) {
            log.warn("Failed to send websocket message. type={}", type, e);
            closeContext(context, CloseStatus.SERVER_ERROR, false);
        }
    }

    private void closeContext(WsContext context, CloseStatus closeStatus, boolean sendServerError) {
        if (!context.markClosing()) {
            return;
        }

        contexts.remove(context.sessionId(), context);
        if (sendServerError) {
            try {
                sendError(context.frontendSession(), ErrorCode.SERVER_ERROR);
            } catch (Exception e) {
                log.debug("Failed to send server error envelope: {}", e.getMessage());
            }
        }

        closeAiSessionQuietly(context);

        try {
            if (context.frontendSession().isOpen()) {
                context.frontendSession().close(closeStatus);
            }
        } catch (Exception e) {
            log.debug("Failed to close frontend websocket session: {}", e.getMessage());
        }
    }

    private void closeAiSessionQuietly(WsContext context) {
        AiRealtimeBridgeClient.AiBridgeSession aiSession = context.aiSession();
        if (aiSession == null) {
            return;
        }
        try {
            aiSession.close();
        } catch (Exception e) {
            log.debug("Failed to close ai websocket session: {}", e.getMessage());
        }
    }

    private static final class WsContext {
        private final String sessionId;
        private final WebSocketSession frontendSession;
        private final Long userId;
        private final Long diaryId;
        private final StringBuilder userTranscript = new StringBuilder();
        private final StringBuilder assistantTranscript = new StringBuilder();
        private final Object lock = new Object();
        private final AtomicBoolean closing = new AtomicBoolean(false);

        private boolean inputFinished;
        private boolean outputFinished;
        private volatile AiRealtimeBridgeClient.AiBridgeSession aiSession;

        private WsContext(String sessionId, WebSocketSession frontendSession, Long userId, Long diaryId) {
            this.sessionId = sessionId;
            this.frontendSession = frontendSession;
            this.userId = userId;
            this.diaryId = diaryId;
        }

        private String sessionId() {
            return sessionId;
        }

        private WebSocketSession frontendSession() {
            return frontendSession;
        }

        private Long userId() {
            return userId;
        }

        private Long diaryId() {
            return diaryId;
        }

        private AiRealtimeBridgeClient.AiBridgeSession aiSession() {
            return aiSession;
        }

        private void setAiSession(AiRealtimeBridgeClient.AiBridgeSession aiSession) {
            this.aiSession = aiSession;
        }

        private boolean markClosing() {
            return closing.compareAndSet(false, true);
        }

        private boolean isClosing() {
            return closing.get();
        }

        private void appendUserTranscript(String text) {
            append(userTranscript, text);
        }

        private void appendAssistantTranscript(String text) {
            append(assistantTranscript, text);
        }

        private void append(StringBuilder target, String text) {
            synchronized (lock) {
                String trimmed = text == null ? null : text.trim();
                if (trimmed == null || trimmed.isBlank()) {
                    return;
                }
                if (!target.isEmpty()) {
                    target.append(' ');
                }
                target.append(trimmed);
            }
        }

        private void markInputFinished() {
            synchronized (lock) {
                inputFinished = true;
            }
        }

        private void markOutputFinished() {
            synchronized (lock) {
                outputFinished = true;
            }
        }

        private boolean isTurnReady() {
            synchronized (lock) {
                return inputFinished && outputFinished;
            }
        }

        private void resetAssistantTranscript() {
            synchronized (lock) {
                assistantTranscript.setLength(0);
                outputFinished = false;
            }
        }

        private TurnSnapshot consumeTurnSnapshot(boolean forced) {
            synchronized (lock) {
                if (!forced && !(inputFinished && outputFinished)) {
                    return null;
                }

                String userText = userTranscript.toString().trim();
                String assistantText = assistantTranscript.toString().trim();

                userTranscript.setLength(0);
                assistantTranscript.setLength(0);
                inputFinished = false;
                outputFinished = false;
                return new TurnSnapshot(userText, assistantText);
            }
        }
    }

    private record TurnSnapshot(String userText, String assistantText) {
    }

    private record WsEnvelope(String type, Object data) {
    }
}
