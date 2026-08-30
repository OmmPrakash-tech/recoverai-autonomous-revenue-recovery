package com.recoverai.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RazorpayPaymentVerificationRequest(

        @JsonProperty("razorpay_order_id")
        String razorpayOrderId,

        @JsonProperty("razorpay_payment_id")
        String razorpayPaymentId,

        @JsonProperty("razorpay_signature")
        String razorpaySignature
) {
}