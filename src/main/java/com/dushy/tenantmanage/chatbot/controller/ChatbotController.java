package com.dushy.tenantmanage.chatbot.controller;

import com.dushy.tenantmanage.chatbot.dto.ChatMessageDto;
import com.dushy.tenantmanage.chatbot.dto.ChatResponseDto;
import com.dushy.tenantmanage.chatbot.service.ChatbotService;
import com.dushy.tenantmanage.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.dushy.tenantmanage.chatbot.entity.ChatbotMessage;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatbotController {

    private final ChatbotService chatbotService;

    /**
     * WebSocket endpoint for handling chat messages
     * Client sends message to /app/chat
     * Response is sent to /topic/chat/{sessionToken}
     */
    @MessageMapping("/chat")
    @SendTo("/topic/chat")
    public ChatResponseDto handleChatMessage(
            ChatMessageDto message,
            SimpMessageHeaderAccessor headerAccessor,
            @AuthenticationPrincipal User user) {
        log.info("Received WebSocket message: {}", message.getMessage());

        if (user == null) {
            log.error("User not authenticated");
            return ChatResponseDto.builder()
                    .message("Authentication required. Please log in.")
                    .intent("error")
                    .confidence(0.0)
                    .build();
        }

        return chatbotService.processMessage(message, user);
    }

    /**
     * REST API endpoint for chat (alternative to WebSocket)
     */
    @PostMapping("/api/chat/message")
    @ResponseBody
    public ChatResponseDto sendMessage(
            @RequestBody ChatMessageDto message,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            log.error("User not authenticated for REST chat endpoint");
            return ChatResponseDto.builder()
                    .message("Authentication required. Please log in.")
                    .intent("error")
                    .confidence(0.0)
                    .build();
        }
        log.info("Received REST message from user {}: {}", user.getId(), message.getMessage());
        return chatbotService.processMessage(message, user);
    }

    /**
     * Get conversation history
     */
    @GetMapping("/api/chat/history")
    @ResponseBody
    public List<ChatbotMessage> getHistory(
            @RequestParam String sessionToken,
            @AuthenticationPrincipal User user) {
        log.info("Fetching chat history for session: {}", sessionToken);
        return chatbotService.getSessionHistory(sessionToken);
    }

    /**
     * End chat session
     */
    @DeleteMapping("/api/chat/session")
    @ResponseBody
    public void endSession(
            @RequestParam String sessionToken,
            @AuthenticationPrincipal User user) {
        log.info("Ending chat session: {}", sessionToken);
        chatbotService.endSession(sessionToken);
    }
}
