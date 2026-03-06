package com.example.heydibe.diary.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RequestMapping
@RestController
public class ConversationController {
    @PostMapping("/api/conversations/sessions")
    public String startSession(@RequestBody String entity) {
        return "started conversation session with entity: " + entity + " - TODO";
    }

    @GetMapping("/api/conversations/sessions/{sessionId}/messages")
    public String getMessages(@PathVariable Long sessionId) {
        return "get messages for conversation session id: " + sessionId + " - TODO";
    }

    @PostMapping("/api/conversations/sessions/{sessionId}/end")
    public String endSession(@PathVariable Long sessionId) {
        return "ended conversation session id: " + sessionId + " - TODO";
    }

    // @MessageMapping("/ws/conversations?token={accessToken}")
    // public String handleWebSocketMessage(@PathVariable String accessToken,
    // @RequestBody String message) {
    // return "received WebSocket message with access token: " + accessToken + " and
    // message: " + message + " - TODO";
    // }
}
