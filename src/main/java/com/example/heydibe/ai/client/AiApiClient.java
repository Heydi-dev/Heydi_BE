package com.example.heydibe.ai.client;

// import com.example.heydibe.ai.dto.request.SessionUpdateRequest;
// import com.example.heydibe.ai.dto.request.SummaryRequest;
// import com.example.heydibe.ai.dto.response.SessionResponse;
// import com.example.heydibe.ai.dto.response.SummaryResponse;
import com.example.heydibe.ai.dto.response.TestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class AiApiClient {

    private final RestClient aiRestClient;

    public TestResponse test() {
        return aiRestClient.get()
                .uri("/api/v1/test-model/test")
                .retrieve()
                .body(TestResponse.class);
    }

    // ### examples (WIP)
    // public SummaryResponse summarize(SummaryRequest request) {
    // return aiRestClient.post()
    // .uri("/api/v1/diaries/summaries")
    // .body(request)
    // .retrieve()
    // .body(SummaryResponse.class);
    // }

    // public SessionResponse updateSession(String sessionId, SessionUpdateRequest
    // request) {
    // return aiRestClient.patch()
    // .uri("/api/v1/conversations/sessions/{sessionId}", sessionId)
    // .body(request)
    // .retrieve()
    // .body(SessionResponse.class);
    // }

    // public void deleteSession(String sessionId) {
    // aiRestClient.delete()
    // .uri("/api/v1/conversations/sessions/{sessionId}", sessionId)
    // .retrieve()
    // .toBodilessEntity();
    // }
}
