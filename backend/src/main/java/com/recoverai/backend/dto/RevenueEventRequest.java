package com.recoverai.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


public record RevenueEventRequest(

        @JsonProperty("event_id")
        String eventId,

        @JsonProperty("razorpay_payment_id")
        String razorpayPaymentId,

        @JsonProperty("customer_id")
        String customerId,

        @JsonProperty("amount")
        double amount,

        @JsonProperty("currency")
        String currency,

        @JsonProperty("event_type")
        String eventType,

        @JsonProperty("reason")
        String reason
) {
}