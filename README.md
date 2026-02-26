# Alon's Chat Bot

Full-stack playground with a Spring Boot backend and a Vite/React frontend. The bot now streams real answers through OpenAI and keeps multiple chat sessions with a modern desktop-style UI.

## Project structure
- `backend/` – Spring Boot 3.5 REST API with OpenAI + chat session management.
- `frontend/` – Vite + React SPA with a pinned header and sidebar for chat history.

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
- `GET /api/chats` → list chat sessions `{ id, title, createdAt, updatedAt }`
- `POST /api/chats` `{ "title": "optional" }` → create a new chat session
- `GET /api/chats/{chatId}/messages` → history for a session
- `POST /api/chats/{chatId}/messages` `{ "message": "..." }` → send a message and get `{ reply, suggestions }`
- Legacy fallback (`/api/messages`) still works and simply targets the first chat.

The backend keeps the last ~80 turns per chat in memory. Swap in a database when you need persistence.
