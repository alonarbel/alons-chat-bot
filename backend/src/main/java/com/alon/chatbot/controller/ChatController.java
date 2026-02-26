package com.alon.chatbot.controller;

import com.alon.chatbot.model.ChatMessage;
import com.alon.chatbot.model.ChatRequest;
import com.alon.chatbot.model.ChatResponse;
import com.alon.chatbot.model.ChatSessionRequest;
import com.alon.chatbot.model.ChatSessionSummary;
import com.alon.chatbot.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chats")
    public List<ChatSessionSummary> listChats() {
        return chatService.listSessions();
    }

    @PostMapping("/chats")
    public ChatSessionSummary createChat(@RequestBody(required = false) ChatSessionRequest request) {
        var title = request == null ? null : request.title();
        return chatService.createSession(title);
    }

    @GetMapping("/chats/{chatId}/messages")
    public ResponseEntity<List<ChatMessage>> history(@PathVariable String chatId) {
        try {
            return ResponseEntity.ok(chatService.getHistory(chatId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/chats/{chatId}/messages")
    public ResponseEntity<ChatResponse> send(@PathVariable String chatId, @RequestBody ChatRequest request) {
        if (request == null || request.message() == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.ok(chatService.handleMessage(chatId, request.message()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // backward-compatible endpoints
    @GetMapping("/messages")
    public List<ChatMessage> legacyHistory() {
        return chatService.getHistory(chatService.getDefaultSessionId());
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatResponse> legacySend(@RequestBody ChatRequest request) {
        if (request == null || request.message() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(chatService.handleMessage(chatService.getDefaultSessionId(), request.message()));
    }
}
