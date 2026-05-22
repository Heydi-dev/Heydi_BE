package com.example.heydibe.ai.service;

import com.example.heydibe.ai.client.AiApiClient;
import com.example.heydibe.ai.dto.response.TestResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiService {

    private final AiApiClient aiApiClient;

    public TestResponse test() {
        return aiApiClient.test();
    }

    public String generateEmotion(String diaryContent) {
        return aiApiClient.generateEmotion(diaryContent);
    }

    public List<String> generateTopics(String diaryContent) {
        return aiApiClient.generateTopics(diaryContent);
    }

    public String generateOneLineDiary(String diaryContent) {
        return aiApiClient.generateOneLineDiary(diaryContent);
    }

    public String generateDiaryContent(List<Map<String, String>> turns) {
        return aiApiClient.generateDiaryContent(turns);
    }

    public JsonNode generateMonthlyPreferences(List<Map<String, String>> entries) {
        return aiApiClient.generateMonthlyPreferences(entries);
    }

    public String generateMonthlyActivityComment(List<Map<String, String>> entries) {
        return aiApiClient.generateMonthlyActivityComment(entries);
    }

    public String generateMonthlyFeedbackComment(List<Map<String, String>> entries) {
        return aiApiClient.generateMonthlyFeedbackComment(entries);
    }
}
