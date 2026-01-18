package com.dushy.tenantmanage.chatbot.service;

import com.dushy.tenantmanage.chatbot.dto.ChatMessageDto;
import com.dushy.tenantmanage.chatbot.dto.ChatResponseDto;
import com.dushy.tenantmanage.chatbot.entity.ChatbotAction;
import com.dushy.tenantmanage.chatbot.entity.ChatbotMessage;
import com.dushy.tenantmanage.chatbot.entity.ChatbotSession;
import com.dushy.tenantmanage.chatbot.repository.ChatbotActionRepository;
import com.dushy.tenantmanage.chatbot.repository.ChatbotMessageRepository;
import com.dushy.tenantmanage.chatbot.repository.ChatbotSessionRepository;
import com.dushy.tenantmanage.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final ChatbotSessionRepository sessionRepository;
    private final ChatbotMessageRepository messageRepository;
    private final ChatbotActionRepository actionRepository;
    private final IntentRecognitionService intentRecognitionService;
    private final EntityExtractionService entityExtractionService;
    private final ActionExecutorService actionExecutorService;
    private final ResponseGeneratorService responseGeneratorService;
    private final ObjectMapper objectMapper;

    /**
     * Main entry point for processing user messages
     *
     * @param messageDto Incoming message
     * @param user       Authenticated user
     * @return Chatbot response
     */
    @Transactional
    public ChatResponseDto processMessage(ChatMessageDto messageDto, User user) {
        long startTime = System.currentTimeMillis();
        log.info("Processing message from user {}: {}", user.getId(), messageDto.getMessage());

        try {
            // 1. Get or create session
            ChatbotSession session = getOrCreateSession(messageDto.getSessionToken(), user, "WEB");

            // 2. Save incoming message
            ChatbotMessage inboundMessage = saveMessage(
                    session.getId(),
                    "INBOUND",
                    messageDto.getMessage(),
                    null,
                    null,
                    null,
                    null);

            // 3. Get conversation context
            List<ChatbotMessage> context = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());

            // 4. Detect intent
            Map<String, Object> intentResult = intentRecognitionService.detectIntent(
                    messageDto.getMessage(),
                    context.size() > 1 ? context.subList(0, context.size() - 1) : List.of());
            String intent = (String) intentResult.get("intent");
            double confidence = (double) intentResult.get("confidence");

            log.info("Detected intent: {} (confidence: {})", intent, confidence);

            // 5. Extract entities
            Map<String, Object> entities = entityExtractionService.extractEntities(
                    messageDto.getMessage(),
                    intent);

            // 6. Execute action
            Map<String, Object> actionResult = actionExecutorService.executeAction(intent, entities, user);

            // 7. Log action
            saveAction(session.getId(), intent, actionResult);

            // 8. Generate response
            String responseText = responseGeneratorService.generateResponse(intent, actionResult);
            List<String> quickActions = responseGeneratorService.generateQuickActions(intent);

            // 9. Save outbound message
            long responseTime = System.currentTimeMillis() - startTime;
            saveMessage(
                    session.getId(),
                    "OUTBOUND",
                    responseText,
                    intent,
                    BigDecimal.valueOf(confidence),
                    entities,
                    (int) responseTime);

            // 10. Update session activity
            session.setLastActivityAt(LocalDateTime.now());
            sessionRepository.save(session);

            // 11. Build response DTO
            return ChatResponseDto.builder()
                    .message(responseText)
                    .sessionToken(session.getSessionToken())
                    .intent(intent)
                    .confidence(confidence)
                    .actions(quickActions)
                    .data((boolean) actionResult.getOrDefault("success", false)
                            ? actionResult.get("data")
                            : null)
                    .build();

        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
            return ChatResponseDto.builder()
                    .message("I apologize, but I encountered an error processing your request. Please try again.")
                    .sessionToken(messageDto.getSessionToken())
                    .intent("error")
                    .confidence(0.0)
                    .build();
        }
    }

    /**
     * Get existing session or create a new one
     */
    private ChatbotSession getOrCreateSession(String sessionToken, User user, String channel) {
        if (sessionToken != null && !sessionToken.isEmpty()) {
            return sessionRepository.findBySessionToken(sessionToken)
                    .filter(ChatbotSession::getIsActive)
                    .map(session -> {
                        session.setLastActivityAt(LocalDateTime.now());
                        return sessionRepository.save(session);
                    })
                    .orElseGet(() -> createNewSession(user, channel));
        }
        return createNewSession(user, channel);
    }

    /**
     * Create a new chat session
     */
    private ChatbotSession createNewSession(User user, String channel) {
        String token = UUID.randomUUID().toString();
        ChatbotSession session = ChatbotSession.builder()
                .userId(user.getId())
                .sessionToken(token)
                .channel(channel)
                .isActive(true)
                .lastActivityAt(LocalDateTime.now())
                .build();

        return sessionRepository.save(session);
    }

    /**
     * Save a message to the database
     */
    private ChatbotMessage saveMessage(
            Long sessionId,
            String direction,
            String messageText,
            String intent,
            BigDecimal confidence,
            Map<String, Object> entities,
            Integer responseTimeMs) {
        try {
            ChatbotMessage message = ChatbotMessage.builder()
                    .sessionId(sessionId)
                    .direction(direction)
                    .messageText(messageText)
                    .intent(intent)
                    .confidence(confidence)
                    .entities(entities != null ? objectMapper.writeValueAsString(entities) : null)
                    .responseTimeMs(responseTimeMs)
                    .build();

            return messageRepository.save(message);
        } catch (Exception e) {
            log.error("Error saving message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save message", e);
        }
    }

    /**
     * Save an action audit log
     */
    private void saveAction(Long sessionId, String intent, Map<String, Object> actionResult) {
        try {
            String actionType = intent.toUpperCase();
            String status = (boolean) actionResult.getOrDefault("success", false) ? "SUCCESS" : "FAILED";

            ChatbotAction action = ChatbotAction.builder()
                    .sessionId(sessionId)
                    .actionType(actionType)
                    .requestPayload(objectMapper.writeValueAsString(actionResult))
                    .responseStatus(status)
                    .build();

            actionRepository.save(action);
        } catch (Exception e) {
            log.error("Error saving action: {}", e.getMessage(), e);
        }
    }

    /**
     * Get conversation history for a session
     */
    public List<ChatbotMessage> getSessionHistory(String sessionToken) {
        return sessionRepository.findBySessionToken(sessionToken)
                .map(session -> messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId()))
                .orElse(List.of());
    }

    /**
     * End a chat session
     */
    @Transactional
    public void endSession(String sessionToken) {
        sessionRepository.findBySessionToken(sessionToken)
                .ifPresent(session -> {
                    session.setIsActive(false);
                    sessionRepository.save(session);
                });
    }
}
