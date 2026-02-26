package com.alon.chatbot.service;

import com.alon.chatbot.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class OpenAiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);

    private final WebClient webClient;
    private final String apiKey;
    private final String model;

    public OpenAiClient(WebClient.Builder builder,
                        @Value("${openai.api.base:https://api.openai.com/v1}") String baseUrl,
                        @Value("${OPENAI_API_KEY:}") String apiKey,
                        @Value("${openai.api.model:gpt-4o-mini}") String model) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public Optional<String> generateReply(List<ChatMessage> history, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("OPENAI_API_KEY is missing – falling back to local responses");
            return Optional.empty();
        }

        var messages = new ArrayList<Message>();
        messages.add(new Message("system",
                "You are Alon's personal day-to-day assistant. Keep replies concise, helpful, and a little playful."
                        + " You can answer in Hebrew when the user writes in Hebrew."));

        history.stream()
                .skip(Math.max(0, history.size() - 10))
                .forEach(entry -> {
                    var role = "bot".equalsIgnoreCase(entry.sender()) ? "assistant" : "user";
                    messages.add(new Message(role, entry.text()));
                });

        messages.add(new Message("user", userMessage));

        var request = new ChatCompletionRequest(model, messages, 0.7);

        try {
            var response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ChatCompletionResponse.class)
                    .timeout(Duration.ofSeconds(20))
                    .onErrorResume(ex -> {
                        log.error("OpenAI call failed", ex);
                        return Mono.empty();
                    })
                    .block();

            if (response == null || response.choices().isEmpty()) {
                return Optional.empty();
            }

            return Optional.ofNullable(response.choices().getFirst().message().content());
        } catch (Exception ex) {
            log.error("Failed to contact OpenAI", ex);
            return Optional.empty();
        }
    }

    private record ChatCompletionRequest(String model, List<Message> messages, double temperature) {
    }

    private record Message(String role, String content) {
    }

    private record ChatCompletionResponse(List<Choice> choices) {
    }

    private record Choice(Message message) {
    }
}
