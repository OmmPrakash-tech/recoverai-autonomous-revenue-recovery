from fastapi import APIRouter

from app.schemas.event_schema import RevenueEvent
from app.services.event_service import EventService


router = APIRouter(
    prefix="/events",
    tags=["Revenue Events"]
)


event_service = EventService()


@router.post("/")
def process_revenue_event(event: RevenueEvent):
    result = event_service.process_event(event)

    return {
        "status": "success",
        "recovery_decision": result
    }