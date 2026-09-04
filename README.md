<div align="center">

# RecoverAI

### Autonomous Revenue Recovery powered by AI

RecoverAI detects failed payment events, verifies them with Razorpay, uses a local AI model to recommend a recovery strategy, applies safety guardrails, and stores every decision in an auditable PostgreSQL workflow.

[![Java](https://img.shields.io/badge/Java-25-E76F00?logo=openjdk&logoColor=white)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![FastAPI](https://img.shields.io/badge/FastAPI-AI_Agent-009688?logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Ollama](https://img.shields.io/badge/Ollama-Qwen3_8B-black?logo=ollama&logoColor=white)](https://ollama.com/)

</div>

## Overview

RecoverAI is a full-stack revenue-operations platform for handling failed digital payments. It combines verified Razorpay payment data with an AI decision engine and deterministic business rules to choose a safe recovery action.

The platform includes:

- A React dashboard for monitoring, searching, filtering, approving, and rejecting recovery events.
- A Spring Boot REST API that coordinates payment verification and the complete recovery workflow.
- A FastAPI AI service that sends structured payment context to Qwen3 8B through Ollama.
- A rule-based fallback when the local language model is unavailable or returns an invalid decision.
- Policy guardrails that prevent unsafe or high-value recovery actions from running automatically.
- A PostgreSQL audit trail containing the event, AI recommendation, policy decision, and final status.

## Key Features

- Razorpay order creation and payment-signature verification.
- Verification of failed-payment status, amount, and currency against Razorpay.
- AI-generated recovery actions with validated structured output.
- Six supported strategies: immediate retry, delayed retry, notification, customer action, support escalation, and manual review.
- Automatic fallback decisions for common failure reasons.
- Human-in-the-loop approval for critical or high-value cases.
- Search and filters for event ID, customer ID, payment ID, priority, policy decision, and status.
- Dashboard statistics for processed, auto-approved, blocked, and manual-review events.
- Persistent event history for auditing and operational review.

## Project Architecture

<p align="center">
  <img src="docs/recoverai_architecture.png" alt="RecoverAI project architecture" width="100%" />
</p>

### Recovery Flow

1. Razorpay produces a payment or revenue event.
2. `RevenueEventController` receives the request through the Spring Boot REST API.
3. `RazorpayPaymentService` verifies the payment ID, failed status, amount, and currency.
4. `RecoveryWorkflowService` creates an audit record and sends the event to the FastAPI AI service.
5. `RecoveryAgent` asks Qwen3 8B, running locally through Ollama, for a recovery action and priority.
6. If the model is unavailable or its response is invalid, a deterministic rule-based fallback is used.
7. `PolicyGuardrailService` checks the recommendation before execution.
8. Allowed actions are handled by `RecoveryActionService`; blocked actions wait for human review.
9. The final decision and status are saved in PostgreSQL and exposed to the React dashboard.

## Technology Stack

| Layer | Technology | Purpose |
| --- | --- | --- |
| Frontend | React 19, Vite 8, CSS | Recovery operations dashboard |
| Backend | Java 25, Spring Boot 4.1.1 | REST API and workflow orchestration |
| AI service | Python, FastAPI, Pydantic | AI-agent API and response validation |
| Local AI | Ollama, Qwen3 8B | Recovery action and priority recommendation |
| Payments | Razorpay Java SDK | Orders, payment lookup, and signature verification |
| Database | PostgreSQL, Spring Data JPA | Recovery-event persistence and audit trail |
| Testing | JUnit 5 | Backend policy tests |

## Repository Structure

```text
recoverai-autonomous-revenue-recovery/
├── ai-agent/
│   ├── app/
│   │   ├── agents/          # Qwen/Ollama decision agent
│   │   ├── api/routes/      # FastAPI routes
│   │   ├── schemas/         # Request validation models
│   │   └── services/        # AI event-processing service
│   └── requirements.txt
├── backend/
│   ├── src/main/java/       # Controllers, services, DTOs, entities, repository
│   ├── src/main/resources/  # Configuration and Razorpay demo page
│   └── src/test/            # JUnit tests
├── frontend/
│   ├── src/                 # React dashboard
│   └── package.json
├── docs/
│   └── recoverai_architecture.png
└── README.md
```

## Prerequisites

Install the following before running the project:

- Java Development Kit 25
- Python 3.10 or later
- Node.js 20.19+ or 22.12+
- PostgreSQL
- Ollama
- A Razorpay test account and API keys

## Local Setup

### 1. Clone the repository

```bash
git clone https://github.com/OmmPrakash-tech/recoverai-autonomous-revenue-recovery.git
cd recoverai-autonomous-revenue-recovery
```

### 2. Create the PostgreSQL database

Open PostgreSQL and run:

```sql
CREATE DATABASE recoverai;
```

The backend currently connects with the PostgreSQL user `postgres` on port `5432`. You can change these values in `backend/src/main/resources/application.properties`.

### 3. Configure the backend

Create `backend/.env` from the example file:

```env
RAZORPAY_KEY_ID=your_test_key_id
RAZORPAY_KEY_SECRET=your_test_key_secret
DB_PASSWORD=your_postgresql_password
```

Never commit the completed `.env` file or expose real Razorpay credentials.

### 4. Start Ollama

Download the model once:

```bash
ollama pull qwen3:8b
```

Ensure Ollama is running at `http://127.0.0.1:11434`.

### 5. Start the AI agent

```bash
cd ai-agent
python -m venv .venv
```

Activate the virtual environment:

```bash
# Windows PowerShell
.venv\Scripts\Activate.ps1

# macOS or Linux
source .venv/bin/activate
```

Install the dependencies and run FastAPI:

```bash
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

Useful URLs:

- Health check: `http://127.0.0.1:8000/health`
- Interactive API docs: `http://127.0.0.1:8000/docs`

### 6. Start the Spring Boot backend

Open a new terminal:

```bash
cd backend
```

```bash
# Windows
mvnw.cmd spring-boot:run

# macOS or Linux
./mvnw spring-boot:run
```

The backend starts at `http://localhost:8080`.

For a Razorpay checkout demonstration, open:

```text
http://localhost:8080/payment.html
```

### 7. Start the React dashboard

Open another terminal:

```bash
cd frontend
npm ci
npm run dev
```

Open `http://localhost:5173`. Vite proxies `/api` requests to the Spring Boot backend on port `8080`.

## Service Ports

| Service | Default port |
| --- | ---: |
| React dashboard | `5173` |
| Spring Boot API | `8080` |
| FastAPI AI agent | `8000` |
| Ollama | `11434` |
| PostgreSQL | `5432` |

## API Reference

### Spring Boot API

Base URL: `http://localhost:8080/api/v1/revenue`

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/orders?amount={amount}&currency=INR` | Create a Razorpay order |
| `POST` | `/payments/verify` | Verify a Razorpay Checkout signature |
| `POST` | `/events` | Verify and process a failed-payment event |
| `GET` | `/events` | Retrieve the complete recovery audit trail |
| `GET` | `/events/{eventId}` | Retrieve one recovery event |
| `POST` | `/events/{eventId}/approve` | Approve a blocked recovery event |
| `POST` | `/events/{eventId}/reject` | Reject a recovery event |

### Example Recovery Request

```bash
curl -X POST http://localhost:8080/api/v1/revenue/events \
  -H "Content-Type: application/json" \
  -d '{
    "event_id": "evt_1001",
    "razorpay_payment_id": "pay_example_failed_id",
    "customer_id": "cus_501",
    "amount": 1499.00,
    "currency": "INR",
    "event_type": "payment_failed",
    "reason": "network_error"
  }'
```

> The payment ID must refer to a failed Razorpay payment whose amount and currency match the request.

### Example AI Decision

```json
{
  "status": "success",
  "recovery_decision": {
    "event_id": "evt_1001",
    "customer_id": "cus_501",
    "amount": 1499.0,
    "recommended_action": "retry_immediately",
    "priority": "high"
  }
}
```

## Recovery Strategies

| Failure context | Recommended action | Typical priority |
| --- | --- | --- |
| Network or temporary failure | `retry_immediately` | High |
| Insufficient funds | `retry_later` | Medium |
| Authentication problem | `request_customer_action` | High |
| Duplicate or suspicious payment | `escalate_to_support` | Critical |
| Generic failure | `send_notification` | Model-selected |
| Unknown or risky failure | `manual_review` | Medium |

## Policy Guardrails

The Spring Boot policy layer validates every AI recommendation before an action is executed.

| Rule | Result |
| --- | --- |
| Amount is greater than ₹10,000 | Blocked; human approval required |
| Priority is `critical` | Blocked; human approval required |
| Action is missing | Blocked; manual review required |
| Action is unsupported | Blocked; manual review required |
| Supported action is within policy | Automatically approved |

The AI model recommends an action, but the deterministic policy layer makes the final execution decision.

## Testing

Run the backend test suite:

```bash
cd backend

# Windows
mvnw.cmd test

# macOS or Linux
./mvnw test
```

Check and build the frontend:

```bash
cd frontend
npm ci
npm run lint
npm run build
```

## Security Notes

- Keep `.env` files and API secrets out of version control.
- Use Razorpay test credentials during development.
- Do not trust AI output directly; retain the policy guardrail before every recovery action.
- Add authentication and role-based authorization before deploying the dashboard publicly.
- Use HTTPS, secret management, webhook-signature verification, rate limiting, and restricted CORS in production.

## Current Scope

RecoverAI is an educational prototype. `RecoveryActionService` currently records or simulates recovery outcomes; production deployment would require real retry scheduling, messaging, support-ticket, authentication, monitoring, and webhook integrations.

## Future Improvements

- Docker Compose for one-command local startup.
- Razorpay webhook ingestion with idempotency protection.
- JWT authentication and role-based dashboard access.
- Real notification, retry scheduler, and support-ticket integrations.
- Configurable guardrails and approval limits.
- Observability with structured logs, metrics, and distributed tracing.
- Broader automated testing across backend, AI agent, and frontend.
- CI/CD deployment pipeline.

## Author

Developed by [Omm Prakash](https://github.com/OmmPrakash-tech).

If this project is useful, consider giving the repository a star.
