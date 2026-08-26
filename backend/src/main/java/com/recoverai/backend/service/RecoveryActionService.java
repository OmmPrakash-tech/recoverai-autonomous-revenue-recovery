package com.recoverai.backend.service;

import org.springframework.stereotype.Service;

@Service
public class RecoveryActionService {

    public String executeAction(String action) {

        return switch (action) {

            case "retry_immediately" -> {
                System.out.println("Executing immediate payment retry...");
                yield "RETRY_EXECUTED";
            }

            case "retry_later" -> {
                System.out.println("Scheduling payment retry...");
                yield "RETRY_SCHEDULED";
            }

            case "send_notification" -> {
                System.out.println("Sending customer payment notification...");
                yield "NOTIFICATION_SENT";
            }

            case "manual_review" -> {
                System.out.println("Sending event for manual review...");
                yield "MANUAL_REVIEW_REQUIRED";
            }

            default -> {
                System.out.println("Unknown recovery action...");
                yield "ACTION_NOT_SUPPORTED";
            }
        };
    }
}