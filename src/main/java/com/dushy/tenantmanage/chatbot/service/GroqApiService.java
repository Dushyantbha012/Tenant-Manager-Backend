package com.dushy.tenantmanage.chatbot.service;

import com.dushy.tenantmanage.chatbot.config.ChatbotConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroqApiService {

    private final WebClient groqWebClient;
    private final ChatbotConfig chatbotConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Send a chat completion request to Groq API
     *
     * @param messages List of conversation messages
     * @return AI response text
     */
    public String chat(List<Map<String, String>> messages) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", chatbotConfig.getGroqModel(),
                    "messages", messages,
                    "temperature", chatbotConfig.getGroqTemperature(),
                    "max_tokens", chatbotConfig.getGroqMaxTokens());

            Mono<String> responseMono = groqWebClient.post()
                    .uri("")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class);

            String response = responseMono.block();
            JsonNode jsonResponse = objectMapper.readTree(response);

            return jsonResponse.at("/choices/0/message/content").asText();

        } catch (Exception e) {
            log.error("Error calling Groq API: {}", e.getMessage(), e);
            return "I'm having trouble processing your request right now. Please try again.";
        }
    }

    /**
     * Extract structured data using LLM
     *
     * @param userMessage      The user's message
     * @param extractionPrompt The prompt for extraction
     * @return Extracted JSON data
     */
    public String extractStructuredData(String userMessage, String extractionPrompt) {
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", extractionPrompt),
                Map.of("role", "user", "content", userMessage));

        return chat(messages);
    }
}
