from typing import Literal
from pydantic import BaseModel


class RevenueEvent(BaseModel):
    event_id: str
    customer_id: str
    amount: float
    currency: str = "INR"

    event_type: Literal[
        "payment_failed",
        "payment_pending",
        "subscription_cancelled"
    ]

    reason: str