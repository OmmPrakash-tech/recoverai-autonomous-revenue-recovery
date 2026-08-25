package com.recoverai.backend.service;

import com.recoverai.backend.dto.RecoveryDecisionResponse;
import com.recoverai.backend.dto.RevenueEventRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AiAgentService {

    private final RestClient restClient;

    public AiAgentService() {
        this.restClient = RestClient.create();
    }

    public RecoveryDecisionResponse analyzeEvent(
            RevenueEventRequest event
    ) {

        return restClient.post()
                .uri("http://127.0.0.1:8000/api/v1/events/")
                .body(event)
                .retrieve()
                .body(RecoveryDecisionResponse.class);
    }
}