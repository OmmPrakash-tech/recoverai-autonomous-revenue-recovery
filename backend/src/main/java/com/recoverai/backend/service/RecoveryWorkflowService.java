package com.recoverai.backend.service;

import com.recoverai.backend.dto.RecoveryDecisionResponse;
import com.recoverai.backend.dto.RevenueEventRequest;
import com.recoverai.backend.entity.RecoveryEvent;
import com.recoverai.backend.repository.RecoveryEventRepository;
import org.springframework.stereotype.Service;

@Service
public class RecoveryWorkflowService {

    private final RecoveryEventRepository recoveryEventRepository;
    private final AiAgentService aiAgentService;

    public RecoveryWorkflowService(
            RecoveryEventRepository recoveryEventRepository,
            AiAgentService aiAgentService
    ) {
        this.recoveryEventRepository = recoveryEventRepository;
        this.aiAgentService = aiAgentService;
    }

    public RecoveryDecisionResponse processRecovery(
            RevenueEventRequest request
    ) {

        // 1. Create database event
        RecoveryEvent recoveryEvent = new RecoveryEvent();

        recoveryEvent.setEventId(request.eventId());
        recoveryEvent.setCustomerId(request.customerId());
        recoveryEvent.setAmount(request.amount());
        recoveryEvent.setCurrency(request.currency());
        recoveryEvent.setEventType(request.eventType());
        recoveryEvent.setFailureReason(request.reason());
        recoveryEvent.setStatus("PROCESSING");

        // 2. Save event before AI analysis
        recoveryEvent = recoveryEventRepository.save(recoveryEvent);

        // 3. Send event to AI Agent
        RecoveryDecisionResponse decision =
                aiAgentService.analyzeEvent(request);

        // 4. Store AI decision
        recoveryEvent.setRecommendedAction(
                decision.recoveryDecision().recommendedAction()
        );

        recoveryEvent.setPriority(
                decision.recoveryDecision().priority()
        );

        recoveryEvent.setStatus("ANALYZED");

        // 5. Update existing database record
        recoveryEventRepository.save(recoveryEvent);

        // 6. Return AI decision
        return decision;
    }
}