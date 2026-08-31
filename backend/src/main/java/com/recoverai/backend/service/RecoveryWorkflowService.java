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
    private final RazorpayPaymentService razorpayPaymentService;
    private final PolicyGuardrailService policyGuardrailService;

    public RecoveryWorkflowService(
            RecoveryEventRepository recoveryEventRepository,
            AiAgentService aiAgentService,
            RecoveryActionService recoveryActionService,
            RazorpayPaymentService razorpayPaymentService,
            PolicyGuardrailService policyGuardrailService
    ) {
        this.recoveryEventRepository = recoveryEventRepository;
        this.aiAgentService = aiAgentService;
        this.recoveryActionService = recoveryActionService;
        this.razorpayPaymentService = razorpayPaymentService;
        this.policyGuardrailService = policyGuardrailService;
    }

    public RecoveryDecisionResponse processRecovery(
            RevenueEventRequest request
    ) {

        // ==========================================
        // 1. Verify Razorpay payment
        // ==========================================

        boolean paymentValid;

try {
    paymentValid =
            razorpayPaymentService.verifyPayment(
                    request.razorpayPaymentId(),
                    request.amount(),
                    request.currency()
            );
} catch (Exception e) {
    throw new RuntimeException(
            "Unable to verify Razorpay payment: "
                    + e.getMessage(),
            e
    );
}

if (!paymentValid) {
    throw new IllegalStateException(
            "Razorpay payment verification failed"
    );
}

        // ==========================================
        // 3. Create Recovery Event
        // ==========================================

        RecoveryEvent recoveryEvent =
                new RecoveryEvent();

        recoveryEvent.setEventId(
                request.eventId()
        );

        recoveryEvent.setRazorpayPaymentId(
                request.razorpayPaymentId()
        );

        recoveryEvent.setCustomerId(
                request.customerId()
        );

        recoveryEvent.setAmount(
                request.amount()
        );

        recoveryEvent.setCurrency(
                request.currency()
        );

        recoveryEvent.setEventType(
                request.eventType()
        );

        recoveryEvent.setFailureReason(
                request.reason()
        );

        recoveryEvent.setStatus(
                "PROCESSING"
        );

        // ==========================================
        // 4. Save event before AI analysis
        // ==========================================

        recoveryEvent =
                recoveryEventRepository.save(
                        recoveryEvent
                );

        // ==========================================
        // 5. Ask AI Agent for recovery decision
        // ==========================================

        RecoveryDecisionResponse decision =
                aiAgentService.analyzeEvent(
                        request
                );

        // ==========================================
        // 6. Extract AI decision
        // ==========================================

        String recommendedAction =
                decision.recoveryDecision()
                        .recommendedAction();

        String priority =
                decision.recoveryDecision()
                        .priority();

        // ==========================================
        // 7. Store AI decision
        // ==========================================

        recoveryEvent.setRecommendedAction(
                recommendedAction
        );

        recoveryEvent.setPriority(
                priority
        );

        recoveryEvent.setStatus(
                "ANALYZED"
        );

        recoveryEventRepository.save(
                recoveryEvent
        );

       // ==========================================
// 8. Apply policy guardrails
// ==========================================

PolicyGuardrailService.PolicyDecision policy =
        policyGuardrailService.evaluate(
                recommendedAction,
                priority,
                request.amount()
        );

recoveryEvent.setPolicyDecision(
        policy.policyDecision()
);

recoveryEvent.setGateStatus(
        policy.gateStatus()
);

recoveryEvent.setDecisionReason(
        policy.reason()
);

// ==========================================
// 9. Execute only if policy allows
// ==========================================

String actionStatus;

if ("ALLOWED".equals(policy.policyDecision())) {

    actionStatus =
            recoveryActionService.executeAction(
                    recommendedAction
            );

} else {

    actionStatus = "HUMAN_APPROVAL_REQUIRED";

}

// ==========================================
// 10. Store final status
// ==========================================

recoveryEvent.setStatus(
        actionStatus
);

recoveryEventRepository.save(
        recoveryEvent
);

        // ==========================================
        // 11. Return decision response
        // ==========================================

        return decision;
    }
}