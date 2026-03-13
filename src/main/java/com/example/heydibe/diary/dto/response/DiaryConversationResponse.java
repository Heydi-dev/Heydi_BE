package com.example.heydibe.diary.dto.response;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.heydibe.diary.entity.DiaryConversation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiaryConversationResponse {
    private List<MessageResponse> messages;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageResponse {
        private String role;
        private String text;
        private String createdAt;

        public static MessageResponse from(DiaryConversation conversation) {
            return new MessageResponse(
                    conversation.getSender(),
                    conversation.getMessageText(),
                    conversation.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        }
    }
}
