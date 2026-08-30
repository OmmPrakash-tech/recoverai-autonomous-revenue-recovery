package com.recoverai.backend.controller;

import com.recoverai.backend.dto.RecoveryDecisionResponse;
import com.recoverai.backend.dto.RevenueEventRequest;
import com.recoverai.backend.service.RecoveryWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/revenue")
public class RevenueEventController {

    private final RecoveryWorkflowService recoveryWorkflowService;

    public RevenueEventController(
            RecoveryWorkflowService recoveryWorkflowService
    ) {
        this.recoveryWorkflowService = recoveryWorkflowService;
    }

    @PostMapping("/events")
    public ResponseEntity<RecoveryDecisionResponse> processEvent(
            @RequestBody RevenueEventRequest event
    ) {

        RecoveryDecisionResponse decision =
                recoveryWorkflowService.processRecovery(event);

        return ResponseEntity.ok(decision);
    }
}