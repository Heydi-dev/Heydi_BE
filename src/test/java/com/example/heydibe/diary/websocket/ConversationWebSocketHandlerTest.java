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
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

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
    void turnComplete_savesUserAndAssistantMessages() throws Exception {
        // 성공: 스트리밍 텍스트를 합쳐 TURN_COMPLETE 시점에 user/assistant 메시지를 저장한다.
        ConversationWebSocketHandler handler =
                new ConversationWebSocketHandler(new ObjectMapper(), conversationSessionService);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", 1L);
        attributes.put("diaryId", 55L);

        when(session.getId()).thenReturn("ws-1");
        when(session.getAttributes()).thenReturn(attributes);
        when(conversationSessionService.getOwnedActiveSession(1L, 55L)).thenReturn(new Diary());

        handler.afterConnectionEstablished(session);
        handler.handleMessage(session, new TextMessage("{\"type\":\"INPUT_TRANSCRIPT\",\"data\":\"오늘\"}"));
        handler.handleMessage(session, new TextMessage("{\"type\":\"INPUT_TRANSCRIPT\",\"data\":\"너무 피곤했어\"}"));
        handler.handleMessage(session, new TextMessage("{\"type\":\"OUTPUT_TRANSCRIPT\",\"data\":\"오늘 정말 고생 많았어요\"}"));
        handler.handleMessage(session, new TextMessage("{\"type\":\"TURN_COMPLETE\",\"data\":true}"));

        verify(conversationSessionService).saveConversationMessage(1L, 55L, "user", "오늘 너무 피곤했어");
        verify(conversationSessionService).saveConversationMessage(1L, 55L, "assistant", "오늘 정말 고생 많았어요");

        ArgumentCaptor<WebSocketMessage<?>> messageCaptor = ArgumentCaptor.forClass(WebSocketMessage.class);
        verify(session, atLeastOnce()).sendMessage(messageCaptor.capture());
        List<WebSocketMessage<?>> sentMessages = messageCaptor.getAllValues();
        TextMessage lastMessage = (TextMessage) sentMessages.get(sentMessages.size() - 1);
        assertThat(lastMessage.getPayload()).contains("\"type\":\"TURN_COMPLETE\"");

        verify(conversationSessionService).getOwnedActiveSession(eq(1L), eq(55L));
        verify(session, atLeastOnce()).sendMessage(any(TextMessage.class));
    }

    // ==================== 실패 케이스 ====================

    @Test
    void unsupportedMessageType_sendsErrorEnvelope() throws Exception {
        // 실패: 지원하지 않는 type 수신 시 ERROR(code=400) 응답을 전송한다.
        ObjectMapper objectMapper = new ObjectMapper();
        ConversationWebSocketHandler handler = new ConversationWebSocketHandler(objectMapper, conversationSessionService);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", 1L);
        attributes.put("diaryId", 55L);

        when(session.getId()).thenReturn("ws-2");
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
        // 실패: 연결 속성(userId/diaryId)이 없으면 ERROR(code=401) 후 세션을 종료한다.
        ObjectMapper objectMapper = new ObjectMapper();
        ConversationWebSocketHandler handler = new ConversationWebSocketHandler(objectMapper, conversationSessionService);

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
}