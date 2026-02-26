package com.alon.chatbot.controller;

import com.alon.chatbot.model.ChatMessage;
import com.alon.chatbot.model.ChatRequest;
import com.alon.chatbot.model.ChatResponse;
import com.alon.chatbot.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public List<ChatMessage> history() {
        return chatService.getHistory();
    }

    @PostMapping
    public ResponseEntity<ChatResponse> send(@RequestBody ChatRequest request) {
        if (request == null || request.message() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(chatService.handleMessage(request.message()));
    }
}
