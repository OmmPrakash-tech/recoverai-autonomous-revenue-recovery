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
    private final RecoveryActionService recoveryActionService;

    public RecoveryWorkflowService(
            RecoveryEventRepository recoveryEventRepository,
            AiAgentService aiAgentService,
            RecoveryActionService recoveryActionService
    ) {
        this.recoveryEventRepository = recoveryEventRepository;
        this.aiAgentService = aiAgentService;
        this.recoveryActionService = recoveryActionService;
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

        // 3. Ask AI Agent for recovery decision
        RecoveryDecisionResponse decision =
                aiAgentService.analyzeEvent(request);

        String recommendedAction =
                decision.recoveryDecision().recommendedAction();

        // 4. Store AI decision
        recoveryEvent.setRecommendedAction(recommendedAction);

        recoveryEvent.setPriority(
                decision.recoveryDecision().priority()
        );

        recoveryEvent.setStatus("ANALYZED");

        recoveryEventRepository.save(recoveryEvent);

        // 5. Execute the recommended action
        String actionStatus =
                recoveryActionService.executeAction(recommendedAction);

        // 6. Store final action status
        recoveryEvent.setStatus(actionStatus);

        recoveryEventRepository.save(recoveryEvent);

        // 7. Return the AI decision
        return decision;
    }
}