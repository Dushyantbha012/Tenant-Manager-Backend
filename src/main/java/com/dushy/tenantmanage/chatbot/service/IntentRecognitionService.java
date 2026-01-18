package com.dushy.tenantmanage.chatbot.service;

import com.dushy.tenantmanage.chatbot.config.ChatbotConfig;
import com.dushy.tenantmanage.chatbot.entity.ChatbotMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntentRecognitionService {

    private final GroqApiService groqApiService;
    private final ChatbotConfig chatbotConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Detect intent from user message using LLM
     *
     * @param message User's message
     * @param context Previous messages for context
     * @return Map with "intent" and "confidence" keys
     */
    public Map<String, Object> detectIntent(String message, List<ChatbotMessage> context) {
        String systemPrompt = buildIntentSystemPrompt();
        String userPrompt = buildUserPromptWithContext(message, context);

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt));

        String response = groqApiService.chat(messages);

        return parseIntentResponse(response);
    }

    private String buildIntentSystemPrompt() {
        return """
                You are an intent classifier for a Tenant Management System chatbot.
                Analyze the user's message and return ONLY a JSON object with the detected intent and confidence score.

                Available intents:
                - view_properties: User wants to see their properties
                - view_property_details: User wants details of a specific property
                - view_rooms: User wants to see rooms in a property
                - view_tenants: User wants to see all tenants
                - view_tenant_details: User wants details of a specific tenant
                - search_tenant: User wants to search for a tenant by name or phone
                - view_due_rent: User wants to see pending rent payments
                - view_payment_history: User wants to see payment history
                - view_summary: User wants dashboard/portfolio summary
                - help: User needs help or doesn't know what to do
                - cancel: User wants to cancel current operation

                Return format (JSON only, no other text):
                {
                  "intent": "intent_name",
                  "confidence": 0.95
                }

                If unsure, set confidence lower than 0.7 and use "help" intent.
                """;
    }

    private String buildUserPromptWithContext(String message, List<ChatbotMessage> context) {
        StringBuilder prompt = new StringBuilder();

        if (context != null && !context.isEmpty()) {
            prompt.append("Previous conversation:\n");
            int contextSize = Math.min(3, context.size()); // Last 3 messages
            for (int i = context.size() - contextSize; i < context.size(); i++) {
                ChatbotMessage msg = context.get(i);
                String role = msg.getDirection().equals("INBOUND") ? "User" : "Bot";
                prompt.append(role).append(": ").append(msg.getMessageText()).append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("Current message: ").append(message);
        return prompt.toString();
    }

    private Map<String, Object> parseIntentResponse(String response) {
        try {
            // Clean up response - remove markdown code blocks if present
            String cleanResponse = response.trim();
            if (cleanResponse.startsWith("```json")) {
                cleanResponse = cleanResponse.substring(7);
            }
            if (cleanResponse.startsWith("```")) {
                cleanResponse = cleanResponse.substring(3);
            }
            if (cleanResponse.endsWith("```")) {
                cleanResponse = cleanResponse.substring(0, cleanResponse.length() - 3);
            }
            cleanResponse = cleanResponse.trim();

            JsonNode jsonNode = objectMapper.readTree(cleanResponse);

            Map<String, Object> result = new HashMap<>();
            result.put("intent", jsonNode.get("intent").asText("help"));
            result.put("confidence", jsonNode.get("confidence").asDouble(0.5));

            log.info("Detected intent: {} with confidence: {}", result.get("intent"), result.get("confidence"));
            return result;

        } catch (Exception e) {
            log.error("Error parsing intent response: {}", e.getMessage(), e);
            // Return default "help" intent on error
            return Map.of("intent", "help", "confidence", 0.3);
        }
    }
}
