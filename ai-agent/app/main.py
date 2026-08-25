from fastapi import FastAPI

from app.api.routes.events import router as events_router


app = FastAPI(
    title="RecoverAI Agent",
    description="AI-powered autonomous revenue recovery service",
    version="0.1.0"
)


app.include_router(
    events_router,
    prefix="/api/v1"
)


@app.get("/")
def root():
    return {
        "service": "RecoverAI AI Agent",
        "status": "running"
    }


@app.get("/health")
def health():
    return {
        "status": "healthy"
    }