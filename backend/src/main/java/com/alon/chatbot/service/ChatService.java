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

    public List<ChatMessage> getHistory() {
        return new ArrayList<>(history);
    }

    public ChatResponse handleMessage(String userMessage) {
        var trimmed = userMessage == null ? "" : userMessage.trim();
        var userEntry = new ChatMessage("user", trimmed, Instant.now());
        addToHistory(userEntry);

        String reply = buildReply(trimmed);
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

    private String buildReply(String message) {
        if (message.isBlank()) {
            return "תכתוב לי משהו ואני כבר אתן רעיון";
        }

        String lower = message.toLowerCase();
        if (lower.contains("כרטיס")) {
            return "כדאי לבדוק אם יש הנחה ללקוחות קבועים לפני שאתה סוגר";
        }
        if (lower.contains("טיסה") || lower.contains("חופשה")) {
            return "אוכל להשוות לך מחירים בין כמה אתרים ולהזכיר מתנות דיוטי";
        }
        if (lower.contains("ארוחה") || lower.contains("לאכול")) {
            return "לך על משהו איטלקי היום – פסטה שמנה עם פסטו ולימון";
        }
        if (lower.contains("שלום") || lower.contains("היי")) {
            return "אהלן! איך אפשר לעזור לך היום?";
        }
        return "הבנתי, בוא נפרק את זה לצעדים קטנים ואני אעזור בכל אחד";
    }

    private List<String> buildSuggestions(String message) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("תן לי רעיון לערב" );
        suggestions.add("תזכיר לי משהו חשוב" );
        suggestions.add("בוא נכין רשימת משימות" );

        String lower = message.toLowerCase();
        if (lower.contains("טיסה")) {
            suggestions.set(0, "מצא לי טיסות זולות" );
        }
        if (lower.contains("ארוחה")) {
            suggestions.set(0, "מה לאכול הערב?" );
        }
        return suggestions;
    }
}
