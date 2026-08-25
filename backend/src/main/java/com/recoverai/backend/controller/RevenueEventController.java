package com.recoverai.backend.controller;

import com.recoverai.backend.dto.RecoveryDecisionResponse;
import com.recoverai.backend.dto.RevenueEventRequest;
import com.recoverai.backend.service.AiAgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/revenue")
public class RevenueEventController {

    private final AiAgentService aiAgentService;

    public RevenueEventController(
            AiAgentService aiAgentService
    ) {
        this.aiAgentService = aiAgentService;
    }

    @PostMapping("/events")
    public ResponseEntity<RecoveryDecisionResponse> processEvent(
            @RequestBody RevenueEventRequest event
    ) {

        RecoveryDecisionResponse decision =
                aiAgentService.analyzeEvent(event);

        return ResponseEntity.ok(decision);
    }
}