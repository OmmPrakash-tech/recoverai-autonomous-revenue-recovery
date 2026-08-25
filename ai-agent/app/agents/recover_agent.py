class RecoveryAgent:

    def analyze_event(self, event):
        recovery_strategies = {
            "insufficient_funds": {
                "action": "retry_later",
                "priority": "medium"
            },
            "network_error": {
                "action": "retry_immediately",
                "priority": "high"
            },
            "authentication_error": {
                "action": "request_customer_action",
                "priority": "high"
            },
            "duplicate_charge": {
                "action": "escalate_to_support",
                "priority": "critical"
            }
        }

        strategy = recovery_strategies.get(
            event.reason.lower(),
            {
                "action": "manual_review",
                "priority": "medium"
            }
        )

        return {
            "event_id": event.event_id,
            "customer_id": event.customer_id,
            "amount": event.amount,
            "recommended_action": strategy["action"],
            "priority": strategy["priority"]
        }