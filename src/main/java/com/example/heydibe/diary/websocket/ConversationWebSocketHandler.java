package com.example.heydibe.diary.websocket;

import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.example.heydibe.common.response.ApiResponse;
import com.example.heydibe.diary.service.ConversationSessionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
            contexts.put(session.getId(), new WsContext(userId, diaryId));
        } catch (CustomException e) {
            sendError(session, e.getErrorCode());
            session.close(CloseStatus.POLICY_VIOLATION);
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
                case TYPE_INPUT_TRANSCRIPT -> appendText(context.userTranscript(), data);
                case TYPE_OUTPUT_TRANSCRIPT -> appendText(context.assistantTranscript(), data);
                case TYPE_TURN_COMPLETE -> {
                    if (asBoolean(data)) {
                        flushTurnMessages(context);
                    }
                    sendEnvelope(session, TYPE_TURN_COMPLETE, true);
                }
                case TYPE_INTERRUPT -> {
                    context.assistantTranscript().setLength(0);
                    sendEnvelope(session, TYPE_INTERRUPT, true);
                }
                default -> sendError(session, ErrorCode.WS_UNSUPPORTED_MESSAGE_TYPE);
            }
        } catch (CustomException e) {
            sendError(session, e.getErrorCode());
            if (e.getErrorCode() == ErrorCode.AUTHENTICATION_EXPIRED
                    || e.getErrorCode() == ErrorCode.DIARY_NOT_FOUND
                    || e.getErrorCode() == ErrorCode.CONVERSATION_ALREADY_ENDED) {
                session.close(CloseStatus.POLICY_VIOLATION);
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

        ByteBuffer payload = message.getPayload();
        byte[] bytes = new byte[payload.remaining()];
        payload.get(bytes);

        String base64 = Base64.getEncoder().encodeToString(bytes);
        sendEnvelope(session, TYPE_AUDIO_CHUNK, base64);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        contexts.remove(session.getId());
    }

    private void appendText(StringBuilder buffer, JsonNode data) {
        String text = data == null ? null : data.asText();
        if (text == null || text.isBlank()) {
            return;
        }
        if (!buffer.isEmpty()) {
            buffer.append(' ');
        }
        buffer.append(text.trim());
    }

    private void flushTurnMessages(WsContext context) {
        String userText = context.userTranscript().toString().trim();
        String assistantText = context.assistantTranscript().toString().trim();

        if (!userText.isEmpty()) {
            conversationSessionService.saveConversationMessage(context.userId(), context.diaryId(), "user", userText);
        }
        if (!assistantText.isEmpty()) {
            conversationSessionService.saveConversationMessage(context.userId(), context.diaryId(), "assistant", assistantText);
        }

        context.userTranscript().setLength(0);
        context.assistantTranscript().setLength(0);
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

    private void sendEnvelope(WebSocketSession session, String type, Object data) throws Exception {
        WsEnvelope envelope = new WsEnvelope(type, data);
        String json = objectMapper.writeValueAsString(envelope);
        session.sendMessage(new TextMessage(json.getBytes(StandardCharsets.UTF_8)));
    }

    private void sendError(WebSocketSession session, ErrorCode errorCode) throws Exception {
        ApiResponse<Void> data = ApiResponse.fail(errorCode.getCode(), errorCode.getMessage());
        sendEnvelope(session, TYPE_ERROR, data);
    }

    private record WsContext(Long userId, Long diaryId, StringBuilder userTranscript, StringBuilder assistantTranscript) {
        private WsContext(Long userId, Long diaryId) {
            this(userId, diaryId, new StringBuilder(), new StringBuilder());
        }
    }

    private record WsEnvelope(String type, Object data) {
    }
}
