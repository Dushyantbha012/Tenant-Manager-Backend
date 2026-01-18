package com.dushy.tenantmanage.chatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntityExtractionService {

    private final GroqApiService groqApiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extract entities from user message based on intent
     *
     * @param message User's message
     * @param intent  Detected intent
     * @return Map of extracted entities
     */
    public Map<String, Object> extractEntities(String message, String intent) {
        Map<String, Object> entities = new HashMap<>();

        // Pattern-based extraction (fast and cheap)
        extractPatternBasedEntities(message, entities);

        // LLM-based extraction for complex entities (only when needed)
        if (needsLlmExtraction(intent, entities)) {
            extractLlmBasedEntities(message, intent, entities);
        }

        log.info("Extracted entities for intent '{}': {}", intent, entities);
        return entities;
    }

    private void extractPatternBasedEntities(String message, Map<String, Object> entities) {
        // Phone number extraction
        Pattern phonePattern = Pattern.compile("\\b\\d{10}\\b|\\+91[\\s-]?\\d{10}");
        Matcher phoneMatcher = phonePattern.matcher(message);
        if (phoneMatcher.find()) {
            entities.put("phone_number", phoneMatcher.group().replaceAll("[\\s-]", ""));
        }

        // Amount extraction (₹5000, 5000 rupees, etc.)
        Pattern amountPattern = Pattern.compile("₹?\\s*(\\d+(?:,\\d+)*)(?:\\s*rupees?)?", Pattern.CASE_INSENSITIVE);
        Matcher amountMatcher = amountPattern.matcher(message);
        if (amountMatcher.find()) {
            String amountStr = amountMatcher.group(1).replace(",", "");
            entities.put("amount", Double.parseDouble(amountStr));
        }

        // Room number extraction
        Pattern roomPattern = Pattern.compile("\\broom\\s+(\\w+)|\\b(\\d{3}|[A-Z]-\\d+)\\b", Pattern.CASE_INSENSITIVE);
        Matcher roomMatcher = roomPattern.matcher(message);
        if (roomMatcher.find()) {
            entities.put("room_number", roomMatcher.group(1) != null ? roomMatcher.group(1) : roomMatcher.group(2));
        }

        // Property ID extraction
        Pattern propertyIdPattern = Pattern.compile("property\\s+(\\d+)|building\\s+#?(\\d+)",
                Pattern.CASE_INSENSITIVE);
        Matcher propertyIdMatcher = propertyIdPattern.matcher(message);
        if (propertyIdMatcher.find()) {
            String idStr = propertyIdMatcher.group(1) != null ? propertyIdMatcher.group(1) : propertyIdMatcher.group(2);
            entities.put("property_id", Long.parseLong(idStr));
        }

        // Date keywords
        String lowerMessage = message.toLowerCase();
        if (lowerMessage.contains("today")) {
            entities.put("date", "today");
        } else if (lowerMessage.contains("yesterday")) {
            entities.put("date", "yesterday");
        } else if (lowerMessage.contains("this month")) {
            entities.put("month", "current");
        } else if (lowerMessage.contains("last month")) {
            entities.put("month", "previous");
        }
    }

    private boolean needsLlmExtraction(String intent, Map<String, Object> entities) {
        // Use LLM extraction for intents that need complex entity recognition
        return switch (intent) {
            case "view_property_details", "search_tenant", "view_tenant_details" ->
                !entities.containsKey("property_name") && !entities.containsKey("tenant_name");
            case "record_payment" ->
                !entities.containsKey("tenant_name") && !entities.containsKey("amount");
            default -> false;
        };
    }

    private void extractLlmBasedEntities(String message, String intent, Map<String, Object> entities) {
        String systemPrompt = buildEntityExtractionPrompt(intent);

        String response = groqApiService.extractStructuredData(message, systemPrompt);

        try {
            // Clean up response
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

            // Merge LLM-extracted entities using proper iteration
            jsonNode.fieldNames().forEachRemaining(fieldName -> {
                JsonNode value = jsonNode.get(fieldName);
                if (!value.isNull() && !value.asText().isEmpty()) {
                    entities.put(fieldName, value.asText());
                }
            });

        } catch (Exception e) {
            log.error("Error extracting entities with LLM: {}", e.getMessage(), e);
        }
    }

    private String buildEntityExtractionPrompt(String intent) {
        return String.format("""
                Extract entities from the user's message for the intent: %s

                Return ONLY a JSON object with the extracted entities.
                If an entity is not found, omit it from the response.

                Possible entities:
                - property_name: Name of the property (e.g., "Marine Heights", "Sunset Apartments")
                - tenant_name: Full name of the tenant (e.g., "John Doe", "Raj Sharma")
                - payment_mode: Mode of payment (cash, upi, bank_transfer, card)

                Example response:
                {
                  "property_name": "Marine Heights",
                  "tenant_name": "John Doe"
                }

                Return JSON only, no other text.
                """, intent);
    }
}
