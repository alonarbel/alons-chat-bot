package com.alon.chatbot.service;

import com.alon.chatbot.model.ChatMessage;
import com.alon.chatbot.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Service
public class ChatService {

    private static final int HISTORY_LIMIT = 50;
    private final Deque<ChatMessage> history = new ArrayDeque<>();
    private final OpenAiClient openAiClient;

    public ChatService(OpenAiClient openAiClient) {
        this.openAiClient = openAiClient;
    }

    public List<ChatMessage> getHistory() {
        return new ArrayList<>(history);
    }

    public ChatResponse handleMessage(String userMessage) {
        var trimmed = userMessage == null ? "" : userMessage.trim();
        var userEntry = new ChatMessage("user", trimmed, Instant.now());
        addToHistory(userEntry);

        String reply = openAiClient.generateReply(getHistory(), trimmed)
                .orElseGet(() -> fallbackReply(trimmed));
        var botEntry = new ChatMessage("bot", reply, Instant.now());
        addToHistory(botEntry);

        return new ChatResponse(reply, buildSuggestions(trimmed));
    }

    private void addToHistory(ChatMessage message) {
        history.addLast(message);
        while (history.size() > HISTORY_LIMIT) {
            history.removeFirst();
        }
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
            suggestions.set(0, "What should I eat tonight?" );
        }
        return suggestions;
    }
}
