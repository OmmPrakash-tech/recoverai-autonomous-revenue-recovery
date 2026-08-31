package com.recoverai.backend.controller;


import com.razorpay.Order;
import com.razorpay.RazorpayException;
import com.recoverai.backend.dto.RazorpayOrderResponse;
import com.recoverai.backend.dto.RazorpayPaymentVerificationRequest;
import com.recoverai.backend.dto.RecoveryDecisionResponse;
import com.recoverai.backend.dto.RevenueEventRequest;
import com.recoverai.backend.entity.RecoveryEvent;
import com.recoverai.backend.repository.RecoveryEventRepository;
import com.recoverai.backend.service.RazorpayPaymentService;
import com.recoverai.backend.service.RecoveryWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/revenue")
public class RevenueEventController {

    private final RazorpayPaymentService razorpayPaymentService;
    private final RecoveryWorkflowService recoveryWorkflowService;
    private final RecoveryEventRepository recoveryEventRepository;

    public RevenueEventController(
            RazorpayPaymentService razorpayPaymentService,
            RecoveryWorkflowService recoveryWorkflowService,
            RecoveryEventRepository recoveryEventRepository
    ) {
        this.razorpayPaymentService = razorpayPaymentService;
        this.recoveryWorkflowService = recoveryWorkflowService;
        this.recoveryEventRepository = recoveryEventRepository;
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

    // --------------------------------------------------
    // Get all recovery events - Audit Trail
    // --------------------------------------------------

    @GetMapping("/events")
    public ResponseEntity<List<RecoveryEvent>> getAllEvents() {

        return ResponseEntity.ok(
                recoveryEventRepository.findAll()
        );
    }

    // --------------------------------------------------
    // Get single recovery event by event ID
    // --------------------------------------------------

    @GetMapping("/events/{eventId}")
    public ResponseEntity<RecoveryEvent> getEvent(
            @PathVariable String eventId
    ) {

        return recoveryEventRepository
                .findByEventId(eventId)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // --------------------------------------------------
// Approve recovery event
// --------------------------------------------------

@PostMapping("/events/{eventId}/approve")
public ResponseEntity<RecoveryEvent> approveEvent(
        @PathVariable String eventId
) {

    return recoveryEventRepository
            .findByEventId(eventId)
            .map(event -> {

                event.setStatus("APPROVED");
                event.setGateStatus("HUMAN_APPROVAL_GRANTED");
                event.setPolicyDecision("ALLOWED");
                event.setDecisionReason(
                        "Recovery action approved by human operator."
                );

                RecoveryEvent saved =
                        recoveryEventRepository.save(event);

                return ResponseEntity.ok(saved);

            })
            .orElseGet(() ->
                    ResponseEntity.notFound().build()
            );
}


// --------------------------------------------------
// Reject recovery event
// --------------------------------------------------

@PostMapping("/events/{eventId}/reject")
public ResponseEntity<RecoveryEvent> rejectEvent(
        @PathVariable String eventId
) {

    return recoveryEventRepository
            .findByEventId(eventId)
            .map(event -> {

                event.setStatus("REJECTED");
                event.setGateStatus("HUMAN_REJECTION");
                event.setPolicyDecision("BLOCKED");
                event.setDecisionReason(
                        "Recovery action rejected by human operator."
                );

                RecoveryEvent saved =
                        recoveryEventRepository.save(event);

                return ResponseEntity.ok(saved);

            })
            .orElseGet(() ->
                    ResponseEntity.notFound().build()
            );
}
}