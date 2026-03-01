package com.alon.chatbot.model.dto;

import java.time.Instant;

public record ChatSessionSummary(String id, String title, Instant createdAt, Instant updatedAt) {}
