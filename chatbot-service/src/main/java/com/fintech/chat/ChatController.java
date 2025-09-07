package com.fintech.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*") // REVIEW: production hardening required - configure specific origins
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * Send chat message and get AI response
     * POST /api/chat/message
     */
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        try {
            // Validate request
            if (request.getUserId() == null) {
                return ResponseEntity.badRequest()
                        .body(new ChatResponse("User ID is required", null, null));
            }
            
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ChatResponse("Message cannot be empty", null, null));
            }

            // Process chat message
            ChatResponse response = chatService.processMessage(request.getUserId(), request.getMessage());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // REVIEW: production logging/privacy rules - log error without exposing sensitive data
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChatResponse("An error occurred while processing your message", null, null));
        }
    }

    /**
     * Get chat history for a user
     * GET /api/chat/history/{userId}
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<ChatLog>> getChatHistory(@PathVariable Long userId) {
        try {
            List<ChatLog> chatHistory = chatService.getChatHistory(userId);
            return ResponseEntity.ok(chatHistory);
        } catch (Exception e) {
            // REVIEW: production logging/privacy rules - log error without exposing sensitive data
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get chat statistics for a user
     * GET /api/chat/stats/{userId}
     */
    @GetMapping("/stats/{userId}")
    public ResponseEntity<ChatStats> getChatStats(@PathVariable Long userId) {
        try {
            ChatStats stats = chatService.getChatStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            // REVIEW: production logging/privacy rules - log error without exposing sensitive data
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     * GET /api/chat/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = Map.of(
            "status", "UP",
            "service", "chatbot-service",
            "timestamp", String.valueOf(System.currentTimeMillis())
        );
        return ResponseEntity.ok(health);
    }

    // Request/Response DTOs
    public static class ChatRequest {
        private Long userId;
        private String message;

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ChatResponse {
        private String answer;
        private List<String> sources;
        private String sanitizedMessage;

        public ChatResponse() {}

        public ChatResponse(String answer, List<String> sources, String sanitizedMessage) {
            this.answer = answer;
            this.sources = sources;
            this.sanitizedMessage = sanitizedMessage;
        }

        // Getters and setters
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
        public List<String> getSources() { return sources; }
        public void setSources(List<String> sources) { this.sources = sources; }
        public String getSanitizedMessage() { return sanitizedMessage; }
        public void setSanitizedMessage(String sanitizedMessage) { this.sanitizedMessage = sanitizedMessage; }
    }
}
