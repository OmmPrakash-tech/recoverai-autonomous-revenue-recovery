package com.recoverai.backend.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayException;
import com.recoverai.backend.dto.RazorpayOrderResponse;
import com.recoverai.backend.dto.RazorpayPaymentVerificationRequest;
import com.recoverai.backend.dto.RecoveryDecisionResponse;
import com.recoverai.backend.dto.RevenueEventRequest;
import com.recoverai.backend.service.RazorpayPaymentService;
import com.recoverai.backend.service.RecoveryWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/revenue")
public class RevenueEventController {

    private final RazorpayPaymentService razorpayPaymentService;
    private final RecoveryWorkflowService recoveryWorkflowService;

    public RevenueEventController(
            RazorpayPaymentService razorpayPaymentService,
            RecoveryWorkflowService recoveryWorkflowService
    ) {
        this.razorpayPaymentService = razorpayPaymentService;
        this.recoveryWorkflowService = recoveryWorkflowService;
    }

    // --------------------------------------------------
    // Process revenue recovery event
    // --------------------------------------------------

    @PostMapping("/events")
    public ResponseEntity<RecoveryDecisionResponse> processEvent(
            @RequestBody RevenueEventRequest event
    ) {

        RecoveryDecisionResponse decision =
                recoveryWorkflowService.processRecovery(event);

        return ResponseEntity.ok(decision);
    }

    // --------------------------------------------------
    // Create Razorpay order
    // --------------------------------------------------

    @PostMapping("/orders")
    public ResponseEntity<RazorpayOrderResponse> createOrder(
            @RequestParam double amount,
            @RequestParam(defaultValue = "INR") String currency
    ) throws RazorpayException {

        String receipt =
                "recoverai_" + System.currentTimeMillis();

        Order order =
                razorpayPaymentService.createOrder(
                        amount,
                        currency,
                        receipt
                );

        RazorpayOrderResponse response =
                new RazorpayOrderResponse(
                        order.get("id"),
                        order.get("amount"),
                        order.get("currency"),
                        razorpayPaymentService.getKeyId()
                );

        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------
    // Verify Razorpay Checkout signature
    // --------------------------------------------------

    @PostMapping("/payments/verify")
    public ResponseEntity<String> verifyPayment(
            @RequestBody RazorpayPaymentVerificationRequest request
    ) {

        try {

            boolean verified =
                    razorpayPaymentService.verifySignature(
                            request.razorpayOrderId(),
                            request.razorpayPaymentId(),
                            request.razorpaySignature()
                    );

            if (!verified) {

                return ResponseEntity.badRequest()
                        .body("Payment verification failed");
            }

            return ResponseEntity.ok(
                    "Payment signature verified successfully"
            );

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body(
                            "Unable to verify payment: "
                                    + e.getMessage()
                    );
        }
    }
}