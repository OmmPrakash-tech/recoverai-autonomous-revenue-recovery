import requests


class RecoveryAgent:

    OLLAMA_URL = "http://127.0.0.1:11434/api/generate"
    MODEL = "qwen3:8b"

    def analyze_event(self, event):

        prompt = f"""
You are a payment recovery decision engine.

Payment:
amount: {event.amount}
currency: {event.currency}
event_type: {event.event_type}
reason: {event.reason}

Choose exactly one action:
retry_immediately
retry_later
send_notification
request_customer_action
escalate_to_support
manual_review

Choose exactly one priority:
low
medium
high
critical

Rules:
- Network or temporary failure -> retry_immediately
- Insufficient funds -> retry_later
- Authentication problem -> request_customer_action
- Duplicate or suspicious payment -> escalate_to_support
- Generic failure -> send_notification
- Unknown or risky failure -> manual_review

Return ONLY JSON:
{{"recommended_action":"...","priority":"..."}}
"""

        try:

            response = requests.post(
    self.OLLAMA_URL,
    json={
        "model": self.MODEL,
        "prompt": prompt,
        "stream": False,
        "think": False,
        "options": {
            "temperature": 0
        }
    },
    timeout=60
)

            response.raise_for_status()

            data = response.json()

            llm_response = data.get("response", "").strip()

            # Remove markdown code fences if Qwen returns them
            llm_response = llm_response.replace(
                "```json", ""
            ).replace(
                "```", ""
            ).strip()

            import json

            decision = json.loads(llm_response)

            allowed_actions = {
                "retry_immediately",
                "retry_later",
                "send_notification",
                "request_customer_action",
                "escalate_to_support",
                "manual_review"
            }

            allowed_priorities = {
                "low",
                "medium",
                "high",
                "critical"
            }

            action = decision.get(
                "recommended_action"
            )

            priority = decision.get(
                "priority"
            )

            if (
                action not in allowed_actions
                or priority not in allowed_priorities
            ):
                raise ValueError(
                    "Invalid AI decision"
                )

            print(
                "Qwen3 decision:",
                action,
                "| priority:",
                priority
            )

            return {
                "event_id": event.event_id,
                "customer_id": event.customer_id,
                "amount": event.amount,
                "recommended_action": action,
                "priority": priority
            }

        except Exception as e:

            print(
                "Qwen3 unavailable or invalid:",
                e
            )

            return self._fallback_strategy(event)

    def _fallback_strategy(self, event):

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

        print(
            "Using rule-based fallback:",
            strategy["action"],
            "| priority:",
            strategy["priority"]
        )

        return {
            "event_id": event.event_id,
            "customer_id": event.customer_id,
            "amount": event.amount,
            "recommended_action": strategy["action"],
            "priority": strategy["priority"]
        }