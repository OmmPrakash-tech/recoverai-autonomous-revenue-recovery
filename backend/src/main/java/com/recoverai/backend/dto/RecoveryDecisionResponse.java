package com.recoverai.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RecoveryDecisionResponse(

        String status,

        @JsonProperty("recovery_decision")
        RecoveryDecision recoveryDecision
) {

    public record RecoveryDecision(

            @JsonProperty("event_id")
            String eventId,

            @JsonProperty("customer_id")
            String customerId,

            double amount,

            @JsonProperty("recommended_action")
            String recommendedAction,

            String priority
    ) {
    }
}