package com.example.heydibe.ai.service;

import com.example.heydibe.ai.client.AiApiClient;
import com.example.heydibe.ai.dto.response.TestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiService {

    private final AiApiClient aiApiClient;

    public TestResponse test() {
        return aiApiClient.test();
    }
}
