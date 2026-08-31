package com.recoverai.backend.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PolicyGuardrailServiceTest {

    private final PolicyGuardrailService guardrail =
            new PolicyGuardrailService();

    @Test
    void normalRetryShouldBeAllowed() {

        var result = guardrail.evaluate(
                "retry_later",
                "medium",
                15000
        );

        assertEquals("ALLOWED", result.policyDecision());
        assertEquals("AUTO_APPROVED", result.gateStatus());
    }

    @Test
    void highValuePaymentShouldBeBlocked() {

        var result = guardrail.evaluate(
                "retry_later",
                "medium",
                20000
        );

        assertEquals("BLOCKED", result.policyDecision());
        assertEquals(
                "HUMAN_APPROVAL_REQUIRED",
                result.gateStatus()
        );
    }

    @Test
    void criticalPriorityShouldRequireHumanApproval() {

        var result = guardrail.evaluate(
                "retry_later",
                "critical",
                15000
        );

        assertEquals("BLOCKED", result.policyDecision());
        assertEquals(
                "HUMAN_APPROVAL_REQUIRED",
                result.gateStatus()
        );
    }

    @Test
    void unknownActionShouldBeBlocked() {

        var result = guardrail.evaluate(
                "do_something_dangerous",
                "medium",
                15000
        );

        assertEquals("BLOCKED", result.policyDecision());
        assertEquals(
                "MANUAL_REVIEW",
                result.gateStatus()
        );
    }

    @Test
    void missingActionShouldBeBlocked() {

        var result = guardrail.evaluate(
                null,
                "medium",
                15000
        );

        assertEquals("BLOCKED", result.policyDecision());
    }
}