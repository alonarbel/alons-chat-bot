package com.alon.chatbot.model;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

public class ChatSession {
    private final String id;
    private String title;
    private final Instant createdAt;
    private Instant updatedAt;
    private final Deque<ChatMessage> messages = new ArrayDeque<>();

    public ChatSession(String title) {
        this.id = UUID.randomUUID().toString();
        this.title = title == null || title.isBlank() ? "Untitled Chat" : title;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Deque<ChatMessage> getMessages() {
        return messages;
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }
}
