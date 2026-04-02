package com.example.heydibe.ai.client;

import com.example.heydibe.ai.dto.response.TestResponse;
import com.example.heydibe.common.error.ErrorCode;
import com.example.heydibe.common.exception.CustomException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AiApiClient {

    private final RestClient aiRestClient;

    @Value("${app.ai-server.endpoints.generate-topic:/api/v1/model/topic}")
    private String generateTopicPath;

    @Value("${app.ai-server.endpoints.generate-emotion:/api/v1/model/emotion}")
    private String generateEmotionPath;

    @Value("${app.ai-server.endpoints.generate-diary:/api/v1/model/diary}")
    private String generateDiaryPath;

    @Value("${app.ai-server.endpoints.generate-summary:/api/v1/model/summary}")
    private String generateSummaryPath;

    public TestResponse test() {
        return aiRestClient.get()
                .uri("/api/v1/test-model/test")
                .retrieve()
                .body(TestResponse.class);
    }

    public String generateDiaryContent(List<Map<String, String>> turns) {
        try {
            JsonNode node = aiRestClient.post()
                    .uri(generateDiaryPath)
                    .body(Map.of("turns", turns))
                    .retrieve()
                    .body(JsonNode.class);

            String diary = readString(node, "diary");
            if (diary == null) {
                throw new CustomException(ErrorCode.SERVER_ERROR);
            }
            return diary;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }
    }

    public String generateEmotion(String diaryContent) {
        try {
            JsonNode node = aiRestClient.post()
                    .uri(generateEmotionPath)
                    .body(Map.of("content", diaryContent))
                    .retrieve()
                    .body(JsonNode.class);

            String emotion = readString(node, "response");
            if (emotion == null) {
                throw new CustomException(ErrorCode.SERVER_ERROR);
            }
            return emotion;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }
    }

    public List<String> generateTopics(String diaryContent) {
        try {
            JsonNode node = aiRestClient.post()
                    .uri(generateTopicPath)
                    .body(Map.of("content", diaryContent))
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode topic = dig(node, "topic");
            if (topic == null) {
                throw new CustomException(ErrorCode.SERVER_ERROR);
            }

            List<String> topics = new ArrayList<>();
            String tag1 = readString(topic, "tag1");
            String tag2 = readString(topic, "tag2");

            if (tag1 != null && !tag1.isBlank()) {
                topics.add(tag1.trim());
            }
            if (tag2 != null && !tag2.isBlank()) {
                topics.add(tag2.trim());
            }

            return topics;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }
    }

    public String generateOneLineDiary(String diaryContent) {
        try {
            JsonNode node = aiRestClient.post()
                    .uri(generateSummaryPath)
                    .body(Map.of("diary", diaryContent))
                    .retrieve()
                    .body(JsonNode.class);

            String summary = readString(node, "summary");
            if (summary == null) {
                throw new CustomException(ErrorCode.SERVER_ERROR);
            }
            return summary;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }
    }

    private JsonNode dig(JsonNode root, String path) {
        if (root == null || path == null || path.isBlank()) {
            return null;
        }

        String[] parts = path.split("\\.");
        JsonNode current = root;
        for (String part : parts) {
            if (current == null || current.isNull() || !current.has(part)) {
                return null;
            }
            current = current.get(part);
        }
        return current;
    }

    private String readString(JsonNode root, String path) {
        JsonNode node = dig(root, path);
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
    }
}
