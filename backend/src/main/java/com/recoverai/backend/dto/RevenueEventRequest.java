package com.recoverai.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RevenueEventRequest(

        @JsonProperty("event_id")
        String eventId,

        @JsonProperty("customer_id")
        String customerId,

        double amount,

        String currency,

        @JsonProperty("event_type")
        String eventType,

        String reason
) {
}