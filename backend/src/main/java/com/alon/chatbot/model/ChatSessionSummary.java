package com.alon.chatbot.model;

import java.time.Instant;

public record ChatSessionSummary(String id, String title, Instant createdAt, Instant updatedAt) {
}
