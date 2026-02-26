package com.alon.chatbot.service;

import com.alon.chatbot.model.ChatMessage;
import com.alon.chatbot.model.ChatResponse;
import com.alon.chatbot.model.ChatSessionSummary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatService {

    private static final int HISTORY_LIMIT = 80;
    private static final DateTimeFormatter TITLE_FORMAT =
            DateTimeFormatter.ofPattern("MMM d · HH:mm").withZone(ZoneId.systemDefault());

    private final OpenAiClient openAiClient;
    private final Map<String, ChatSession> sessions = new LinkedHashMap<>();

    public ChatService(OpenAiClient openAiClient) {
        this.openAiClient = openAiClient;
        var initial = createSession("First chat");
        ensureSession(initial.id());
    }

    public synchronized ChatSessionSummary createSession(String requestedTitle) {
        var id = UUID.randomUUID().toString();
        var now = Instant.now();
        var title = Optional.ofNullable(requestedTitle)
                .filter(t -> !t.isBlank())
                .orElse("Chat • " + TITLE_FORMAT.format(now));
        var session = new ChatSession(id, title, now, new ArrayDeque<>());
        sessions.put(id, session);
        return session.toSummary();
    }

    public synchronized List<ChatSessionSummary> listSessions() {
        return sessions.values().stream()
                .sorted((a, b) -> b.updatedAt().compareTo(a.updatedAt()))
                .map(ChatSession::toSummary)
                .toList();
    }

    public synchronized List<ChatMessage> getHistory(String sessionId) {
        return new ArrayList<>(ensureSession(sessionId).history());
    }

    public synchronized ChatResponse handleMessage(String sessionId, String userMessage) {
        var session = ensureSession(sessionId);
        var trimmed = userMessage == null ? "" : userMessage.trim();
        var userEntry = new ChatMessage("user", trimmed, Instant.now());
        addToHistory(session, userEntry);

        String reply = openAiClient.generateReply(List.copyOf(session.history()), trimmed)
                .orElseGet(() -> fallbackReply(trimmed));
        var botEntry = new ChatMessage("bot", reply, Instant.now());
        addToHistory(session, botEntry);
        session.touch();

        return new ChatResponse(reply, buildSuggestions(trimmed));
    }

    private void addToHistory(ChatSession session, ChatMessage message) {
        session.history().addLast(message);
        while (session.history().size() > HISTORY_LIMIT) {
            session.history().removeFirst();
        }
    }

    public synchronized String getDefaultSessionId() {
        return sessions.keySet().stream().findFirst()
                .orElseGet(() -> createSession("Quick chat").id());
    }

    private ChatSession ensureSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return sessions.values().stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("No sessions available"));
        }
        var session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        return session;
    }

    private String fallbackReply(String message) {
        if (message.isBlank()) {
            return "Say anything and I'll riff with you.";
        }

        String lower = message.toLowerCase();
        if (lower.contains("ticket")) {
            return "Remember to jump in right when sales open – every second counts.";
        }
        if (lower.contains("flight") || lower.contains("trip") || lower.contains("vacation")) {
            return "I can scan a few sites, compare routes, and remind you about duty-free snacks.";
        }
        if (lower.contains("dinner") || lower.contains("eat")) {
            return "Let's keep it easy: roasted veggies, tahini drizzle, and a cold drink.";
        }
        if (lower.contains("hi") || lower.contains("hello") || lower.contains("hey")) {
            return "Hey! What's up?";
        }
        return "Got it. Let's break it into simple steps and I'll guide you through.";
    }

    private List<String> buildSuggestions(String message) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("Give me an evening idea");
        suggestions.add("Set a reminder");
        suggestions.add("Plan a to-do list");

        String lower = message.toLowerCase();
        if (lower.contains("flight")) {
            suggestions.set(0, "Find me cheap flights");
        }
        if (lower.contains("dinner")) {
            suggestions.set(0, "What should I eat tonight?");
        }
        return suggestions;
    }

    private static final class ChatSession {
        private final String id;
        private final String title;
        private final Instant createdAt;
        private Instant updatedAt;
        private final Deque<ChatMessage> history;

        private ChatSession(String id, String title, Instant createdAt, Deque<ChatMessage> history) {
            this.id = id;
            this.title = title;
            this.createdAt = createdAt;
            this.updatedAt = createdAt;
            this.history = history;
        }

        public Deque<ChatMessage> history() {
            return history;
        }

        public Instant updatedAt() {
            return updatedAt;
        }

        void touch() {
            updatedAt = Instant.now();
        }

        ChatSessionSummary toSummary() {
            return new ChatSessionSummary(id, title, createdAt, updatedAt);
        }
    }
}
