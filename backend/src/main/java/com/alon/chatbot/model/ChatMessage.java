package com.alon.chatbot.model;

import java.time.Instant;

public record ChatMessage(String id, String role, String text, Instant timestamp) {}
