package com.recoverai.backend.service;

import org.springframework.stereotype.Service;

@Service
public class PolicyGuardrailService {

    private static final double MAX_AUTO_RECOVERY_AMOUNT = 10000.0;

    public PolicyDecision evaluate(
        String action,
        String priority,
        double amount
) {

    // 1. Action must exist
    if (action == null || action.isBlank()) {
        return new PolicyDecision(
                "BLOCKED",
                "MANUAL_REVIEW",
                "No recovery action was provided."
        );
    }

    // 2. Amount boundary
    if (amount > MAX_AUTO_RECOVERY_AMOUNT) {
        return new PolicyDecision(
                "BLOCKED",
                "HUMAN_APPROVAL_REQUIRED",
                "Payment amount exceeds the automatic recovery limit of ₹10,000."
        );
    }

    // 3. Critical actions require human approval
    if ("critical".equalsIgnoreCase(priority)) {
        return new PolicyDecision(
                "BLOCKED",
                "HUMAN_APPROVAL_REQUIRED",
                "Critical-priority recovery requires human approval."
        );
    }

    // 4. Supported automatic actions
    if ("retry_immediately".equals(action)
            || "retry_later".equals(action)
            || "send_notification".equals(action)
            || "request_customer_action".equals(action)
            || "escalate_to_support".equals(action)
            || "manual_review".equals(action)) {

        return new PolicyDecision(
                "ALLOWED",
                "AUTO_APPROVED",
                "Recovery action is within the automatic recovery policy."
        );
    }

    // 5. Unknown action must never execute
    return new PolicyDecision(
            "BLOCKED",
            "MANUAL_REVIEW",
            "Recovery action is not supported by the policy."
    );
}

    public record PolicyDecision(
            String policyDecision,
            String gateStatus,
            String reason
    ) {
    }
}