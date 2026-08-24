from fastapi import FastAPI

app = FastAPI(title="RecoverAI Agent")

@app.get("/")
def home():
    return {
        "service": "RecoverAI AI Agent",
        "status": "running"
    }