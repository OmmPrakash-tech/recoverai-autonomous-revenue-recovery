package com.recoverai.backend.dto;

public record RazorpayOrderResponse(
        String orderId,
        int amount,
        String currency,
        String keyId
) {
}