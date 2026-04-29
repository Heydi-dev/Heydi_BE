package com.example.heydibe.diary.websocket;

import com.example.heydibe.diary.entity.Diary;
import com.example.heydibe.diary.service.ConversationSessionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationWebSocketHandlerTest {

    @Mock
    private ConversationSessionService conversationSessionService;

    @Mock
    private WebSocketSession session;

    // ==================== 성공 케이스 ====================

    @Test
    void aiFinishedEvents_saveMessagesAndSendTurnComplete() throws Exception {
        // 성공: AI input/output finished 이벤트가 모두 오면 turn 저장 후 TURN_COMPLETE를 전송한다.
        ObjectMapper objectMapper = new ObjectMapper();
        FakeAiRealtimeBridgeClient fakeBridgeClient = new FakeAiRealtimeBridgeClient();
        ConversationWebSocketHandler handler =
                new ConversationWebSocketHandler(objectMapper, conversationSessionService, fakeBridgeClient);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", 1L);
        attributes.put("diaryId", 55L);

        when(session.getId()).thenReturn("ws-1");
        when(session.getAttributes()).thenReturn(attributes);
        when(conversationSessionService.getOwnedActiveSession(1L, 55L)).thenReturn(new Diary());

        handler.afterConnectionEstablished(session);

        fakeBridgeClient.emitText("{\"type\":\"input\",\"transcription\":\"오늘\",\"finished\":false}");
        fakeBridgeClient.emitText("{\"type\":\"input\",\"transcription\":\"너무 피곤했어\",\"finished\":true}");
        fakeBridgeClient.emitText("{\"type\":\"output\",\"transcription\":\"오늘 정말 고생 많았어요\",\"finished\":true}");

        verify(conversationSessionService).saveConversationMessage(1L, 55L, "user", "오늘 너무 피곤했어");
        verify(conversationSessionService).saveConversationMessage(1L, 55L, "assistant", "오늘 정말 고생 많았어요");

        ArgumentCaptor<WebSocketMessage<?>> messageCaptor = ArgumentCaptor.forClass(WebSocketMessage.class);
        verify(session, atLeastOnce()).sendMessage(messageCaptor.capture());
        List<WebSocketMessage<?>> sentMessages = messageCaptor.getAllValues();
        TextMessage lastMessage = (TextMessage) sentMessages.get(sentMessages.size() - 1);
        assertThat(lastMessage.getPayload()).contains("\"type\":\"TURN_COMPLETE\"");
    }

    @Test
    void binaryFromFrontend_isForwardedToAi() throws Exception {
        // 성공: 프론트 raw PCM binary는 AI websocket으로 그대로 전달된다.
        ObjectMapper objectMapper = new ObjectMapper();
        FakeAiRealtimeBridgeClient fakeBridgeClient = new FakeAiRealtimeBridgeClient();
        ConversationWebSocketHandler handler =
                new ConversationWebSocketHandler(objectMapper, conversationSessionService, fakeBridgeClient);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", 1L);
        attributes.put("diaryId", 77L);

        when(session.getId()).thenReturn("ws-2");
        when(session.getAttributes()).thenReturn(attributes);
        when(conversationSessionService.getOwnedActiveSession(1L, 77L)).thenReturn(new Diary());

        handler.afterConnectionEstablished(session);

        byte[] payload = new byte[]{1, 2, 3, 4};
        handler.handleMessage(session, new BinaryMessage(ByteBuffer.wrap(payload)));

        assertThat(fakeBridgeClient.session.binaryPayloads).hasSize(1);
        assertThat(fakeBridgeClient.session.binaryPayloads.get(0)).containsExactly(payload);
    }

    // ==================== 실패 케이스 ====================

    @Test
    void unsupportedMessageType_sendsErrorEnvelope() throws Exception {
        // 실패: 프론트에서 지원하지 않는 type 전송 시 ERROR(code=400)를 반환한다.
        ObjectMapper objectMapper = new ObjectMapper();
        FakeAiRealtimeBridgeClient fakeBridgeClient = new FakeAiRealtimeBridgeClient();
        ConversationWebSocketHandler handler =
                new ConversationWebSocketHandler(objectMapper, conversationSessionService, fakeBridgeClient);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", 1L);
        attributes.put("diaryId", 55L);

        when(session.getId()).thenReturn("ws-3");
        when(session.getAttributes()).thenReturn(attributes);
        when(conversationSessionService.getOwnedActiveSession(1L, 55L)).thenReturn(new Diary());

        handler.afterConnectionEstablished(session);
        handler.handleMessage(session, new TextMessage("{\"type\":\"UNKNOWN_TYPE\",\"data\":true}"));

        ArgumentCaptor<WebSocketMessage<?>> messageCaptor = ArgumentCaptor.forClass(WebSocketMessage.class);
        verify(session, atLeastOnce()).sendMessage(messageCaptor.capture());

        TextMessage lastMessage = (TextMessage) messageCaptor.getAllValues()
                .get(messageCaptor.getAllValues().size() - 1);
        JsonNode root = objectMapper.readTree(lastMessage.getPayload());

        assertThat(root.path("type").asText()).isEqualTo("ERROR");
        assertThat(root.path("data").path("success").asBoolean()).isFalse();
        assertThat(root.path("data").path("code").asText()).isEqualTo("400");
        verify(conversationSessionService, never()).saveConversationMessage(any(), any(), any(), any());
    }

    @Test
    void missingConnectionAttributes_sendsAuthErrorAndCloses() throws Exception {
        // 실패: 핸드셰이크 속성(userId/diaryId) 누락 시 ERROR(code=401) 후 연결을 종료한다.
        ObjectMapper objectMapper = new ObjectMapper();
        FakeAiRealtimeBridgeClient fakeBridgeClient = new FakeAiRealtimeBridgeClient();
        ConversationWebSocketHandler handler =
                new ConversationWebSocketHandler(objectMapper, conversationSessionService, fakeBridgeClient);

        when(session.getAttributes()).thenReturn(Map.of());

        handler.afterConnectionEstablished(session);

        ArgumentCaptor<WebSocketMessage<?>> messageCaptor = ArgumentCaptor.forClass(WebSocketMessage.class);
        verify(session, atLeastOnce()).sendMessage(messageCaptor.capture());
        TextMessage lastMessage = (TextMessage) messageCaptor.getAllValues()
                .get(messageCaptor.getAllValues().size() - 1);

        JsonNode root = objectMapper.readTree(lastMessage.getPayload());
        assertThat(root.path("type").asText()).isEqualTo("ERROR");
        assertThat(root.path("data").path("code").asText()).isEqualTo("401");

        verify(session).close(CloseStatus.POLICY_VIOLATION);
    }

    private static final class FakeAiRealtimeBridgeClient implements AiRealtimeBridgeClient {
        private final FakeAiBridgeSession session = new FakeAiBridgeSession();
        private AiBridgeListener listener;

        @Override
        public AiBridgeSession connect(Long userId, AiBridgeListener listener) {
            this.listener = listener;
            return session;
        }

        private void emitText(String payload) {
            if (listener != null) {
                listener.onText(payload);
            }
        }
    }

    private static final class FakeAiBridgeSession implements AiRealtimeBridgeClient.AiBridgeSession {
        private final List<byte[]> binaryPayloads = new ArrayList<>();

        @Override
        public void sendBinary(byte[] payload) {
            binaryPayloads.add(payload);
        }

        @Override
        public void close() {
            // no-op
        }
    }
}

