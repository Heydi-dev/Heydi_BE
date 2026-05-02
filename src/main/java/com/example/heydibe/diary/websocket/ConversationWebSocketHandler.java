package com.example.heydibe.diary.websocket;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.diary.service.ConversationSessionService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private static final String TYPE_INPUT_TURN_COMMITTED = "INPUT_TURN_COMMITTED";
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

        try {
            switch (type) {
                case TYPE_INTERRUPT -> {
                    context.resetAssistantTranscript();
                    sendEnvelope(session, TYPE_INTERRUPT, true);
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
            case "turn_complete" -> handleAiTurnComplete(context);
            case "interrupt" -> {
                context.resetAssistantTranscript();
                sendEnvelopeSafely(context, TYPE_INTERRUPT, true);
            }
            default -> log.debug("Ignore unsupported AI event type: {}", type);
        }
    }

    private void handleAiTranscript(WsContext context, String eventType, JsonNode root) {
        String transcript = extractTranscript(root);
        boolean hasTranscript = transcript != null && !transcript.isBlank();

        if (hasTranscript) {
            if (TYPE_INPUT_TRANSCRIPT.equals(eventType)) {
                context.appendUserTranscript(transcript);
            } else {
                context.appendAssistantTranscript(transcript);

                if (context.markInputTurnCommitted()) {
                    sendEnvelopeSafely(context, TYPE_INPUT_TURN_COMMITTED, true);
                }
            }

            sendEnvelopeSafely(context, eventType, transcript);
        }
    }

    private void handleAiTurnComplete(WsContext context) {
        completeTurn(context);
        sendEnvelopeSafely(context, TYPE_TURN_COMPLETE, true);
    }

    private void handleAiBinary(WsContext context, byte[] payload) {
        if (context.isClosing()) {
            return;
        }
        if (context.markInputTurnCommitted()) {
            sendEnvelopeSafely(context, TYPE_INPUT_TURN_COMMITTED, true);
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

    private void completeTurn(WsContext context) {
        TurnSnapshot snapshot = context.consumeTurnSnapshot();
        if (snapshot == null) {
            return;
        }

        if (!snapshot.userText().isEmpty()) {
            conversationSessionService.saveConversationMessage(
                    context.userId(), context.diaryId(), "USER", snapshot.userText()
            );
        }
        if (!snapshot.assistantText().isEmpty()) {
            conversationSessionService.saveConversationMessage(
                    context.userId(), context.diaryId(), "AI", snapshot.assistantText()
            );
        }
    }

    private String extractTranscript(JsonNode root) {
        String transcription = root.path("transcription").asText(null);
        if (transcription != null) {
            return transcription;
        }

        JsonNode data = root.get("data");
        if (data != null && data.isTextual()) {
            return data.asText();
        }
        return null;
    }

    private void sendEnvelope(WebSocketSession session, String type, Object data) throws Exception {
        WsEnvelope envelope = new WsEnvelope(type, data, null);
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

        private boolean inputTurnCommitted;
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
                if (text == null || text.isBlank()) {
                    return;
                }
                target.append(text);
            }
        }

        private void resetAssistantTranscript() {
            synchronized (lock) {
                assistantTranscript.setLength(0);
                inputTurnCommitted = false;
            }
        }

        private TurnSnapshot consumeTurnSnapshot() {
            synchronized (lock) {
                String userText = userTranscript.toString().trim();
                String assistantText = assistantTranscript.toString().trim();
                if (userText.isEmpty() && assistantText.isEmpty()) {
                    return null;
                }

                userTranscript.setLength(0);
                assistantTranscript.setLength(0);
                inputTurnCommitted = false;
                return new TurnSnapshot(userText, assistantText);
            }
        }

        private boolean markInputTurnCommitted() {
            synchronized (lock) {
                if (inputTurnCommitted) {
                    return false;
                }
                inputTurnCommitted = true;
                return true;
            }
        }
    }

    private record TurnSnapshot(String userText, String assistantText) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record WsEnvelope(
            String type,
            Object data,
            @JsonProperty("turn_complete") Boolean turnComplete
    ) {
    }
}
