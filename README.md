# Alon's Chat Bot

Full-stack playground with a Spring Boot backend and a Vite/React frontend. The bot now streams real answers through OpenAI so the conversation actually feels alive.

## Project structure
- `backend/` – Spring Boot 3.5 REST API (`/api/messages`) with OpenAI integration.
- `frontend/` – Vite + React single page chat UI.

## Prerequisites
- Java 21+
- Node.js 18+
- An OpenAI API key (saved as `OPENAI_API_KEY` in your shell or `.env`)

## Backend
```bash
cd backend
export OPENAI_API_KEY=sk-...   # or set in your shell profile
./mvnw spring-boot:run
```
Server boots on `http://localhost:8080`.

## Frontend
```bash
cd frontend
cp .env.example .env        # adjust VITE_API_BASE if backend runs elsewhere
npm install
npm run dev                 # default: http://localhost:5173
```

For production builds run `npm run build` and serve the `dist/` folder.

## API quick reference
- `GET /api/messages` – returns the current in-memory history.
- `POST /api/messages` `{ "message": "..." }` – stores your prompt, calls OpenAI, and responds with `{ reply, suggestions }`.

The backend keeps the last 50 turns in memory. Swap in a database when you need persistence.
