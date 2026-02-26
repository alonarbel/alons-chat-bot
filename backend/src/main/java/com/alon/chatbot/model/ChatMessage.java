package com.alon.chatbot.model;

import java.time.Instant;

public record ChatMessage(String sender, String text, Instant timestamp) {
}
