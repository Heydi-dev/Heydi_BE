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

    @Test
    void aiTurnCompleteEvent_savesMessagesAndSendsTurnComplete() throws Exception {
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

        fakeBridgeClient.emitText("{\"type\":\"input\",\"transcription\":\"today \"}");
        fakeBridgeClient.emitText("{\"type\":\"input\",\"transcription\":\"I am tired\"}");
        fakeBridgeClient.emitText("{\"type\":\"output\",\"transcription\":\"Take a good rest\"}");
        fakeBridgeClient.emitText("{\"type\":\"turn_complete\"}");

        verify(conversationSessionService).saveConversationMessage(1L, 55L, "USER", "today I am tired");
        verify(conversationSessionService).saveConversationMessage(1L, 55L, "AI", "Take a good rest");

        ArgumentCaptor<WebSocketMessage<?>> messageCaptor = ArgumentCaptor.forClass(WebSocketMessage.class);
        verify(session, atLeastOnce()).sendMessage(messageCaptor.capture());
        List<WebSocketMessage<?>> sentMessages = messageCaptor.getAllValues();
        TextMessage lastMessage = (TextMessage) sentMessages.get(sentMessages.size() - 1);
        JsonNode lastRoot = objectMapper.readTree(lastMessage.getPayload());

        assertThat(lastRoot.path("type").asText()).isEqualTo("TURN_COMPLETE");
        assertThat(lastRoot.path("data").asBoolean()).isTrue();
    }

    @Test
    void transcriptEventsBeforeTurnCompletion_doNotSaveMessages() throws Exception {
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

        fakeBridgeClient.emitText("{\"type\":\"input\",\"transcription\":\"today\",\"finished\":true}");

        ArgumentCaptor<WebSocketMessage<?>> messageCaptor = ArgumentCaptor.forClass(WebSocketMessage.class);
        verify(session, atLeastOnce()).sendMessage(messageCaptor.capture());
        TextMessage lastMessage = (TextMessage) messageCaptor.getAllValues()
                .get(messageCaptor.getAllValues().size() - 1);
        JsonNode lastRoot = objectMapper.readTree(lastMessage.getPayload());

        assertThat(lastRoot.path("type").asText()).isEqualTo("INPUT_TRANSCRIPT");
        assertThat(lastRoot.path("data").asText()).isEqualTo("today");
        assertThat(lastRoot.path("turn_complete").isMissingNode()).isTrue();
        verify(conversationSessionService, never()).saveConversationMessage(any(), any(), any(), any());
    }

    @Test
    void transcriptWhitespace_isPreservedUntilAccumulatedTurnIsTrimmed() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        FakeAiRealtimeBridgeClient fakeBridgeClient = new FakeAiRealtimeBridgeClient();
        ConversationWebSocketHandler handler =
                new ConversationWebSocketHandler(objectMapper, conversationSessionService, fakeBridgeClient);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", 1L);
        attributes.put("diaryId", 55L);

        when(session.getId()).thenReturn("ws-whitespace");
        when(session.getAttributes()).thenReturn(attributes);
        when(conversationSessionService.getOwnedActiveSession(1L, 55L)).thenReturn(new Diary());

        handler.afterConnectionEstablished(session);

        fakeBridgeClient.emitText("{\"type\":\"input\",\"transcription\":\"  hello  \"}");
        fakeBridgeClient.emitText("{\"type\":\"input\",\"transcription\":\"  world  \"}");
        fakeBridgeClient.emitText("{\"type\":\"turn_complete\"}");

        ArgumentCaptor<WebSocketMessage<?>> messageCaptor = ArgumentCaptor.forClass(WebSocketMessage.class);
        verify(session, atLeastOnce()).sendMessage(messageCaptor.capture());

        JsonNode firstTranscript = objectMapper.readTree(((TextMessage) messageCaptor.getAllValues().get(0)).getPayload());
        assertThat(firstTranscript.path("data").asText()).isEqualTo("  hello  ");
        verify(conversationSessionService).saveConversationMessage(1L, 55L, "USER", "hello    world");
    }

    @Test
    void binaryFromFrontend_isForwardedToAi() throws Exception {
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

    @Test
    void firstAiAudioChunk_sendsInputTurnCommittedOnceBeforeAudioChunk() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        FakeAiRealtimeBridgeClient fakeBridgeClient = new FakeAiRealtimeBridgeClient();
        ConversationWebSocketHandler handler =
                new ConversationWebSocketHandler(objectMapper, conversationSessionService, fakeBridgeClient);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", 1L);
        attributes.put("diaryId", 55L);

        when(session.getId()).thenReturn("ws-4");
        when(session.getAttributes()).thenReturn(attributes);
        when(conversationSessionService.getOwnedActiveSession(1L, 55L)).thenReturn(new Diary());

        handler.afterConnectionEstablished(session);

        fakeBridgeClient.emitBinary(new byte[]{1, 2});
        fakeBridgeClient.emitBinary(new byte[]{3, 4});

        ArgumentCaptor<WebSocketMessage<?>> messageCaptor = ArgumentCaptor.forClass(WebSocketMessage.class);
        verify(session, atLeastOnce()).sendMessage(messageCaptor.capture());

        List<JsonNode> sentRoots = new ArrayList<>();
        for (WebSocketMessage<?> sentMessage : messageCaptor.getAllValues()) {
            TextMessage textMessage = (TextMessage) sentMessage;
            sentRoots.add(objectMapper.readTree(textMessage.getPayload()));
        }

        assertThat(sentRoots.stream().map(root -> root.path("type").asText()).toList())
                .containsExactly("INPUT_TURN_COMMITTED", "AUDIO_CHUNK", "AUDIO_CHUNK");
        assertThat(sentRoots.get(0).path("data").asBoolean()).isTrue();
    }

    @Test
    void unsupportedMessageType_sendsErrorEnvelope() throws Exception {
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

        private void emitBinary(byte[] payload) {
            if (listener != null) {
                listener.onBinary(payload);
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
