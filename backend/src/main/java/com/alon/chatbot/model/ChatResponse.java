package com.alon.chatbot.model;

import java.util.List;

public record ChatResponse(String reply, List<String> suggestions) {
}
