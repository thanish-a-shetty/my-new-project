package com.fintech.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private KnowledgeRepository knowledgeRepository;

    @Autowired
    private VectorDbClient vectorDbClient;

    @Autowired
    private ChatLogRepository chatLogRepository;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String openaiModel;

    @Value("${chat.rate-limit.requests-per-minute:20}")
    private int rateLimitPerMinute;

    @Value("${chat.retrieval.top-k:5}")
    private int topK;

    // Rate limiting per user
    private final Map<Long, List<Long>> userRequestTimes = new ConcurrentHashMap<>();

    // PII detection patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "\\b(\\+?1[-.]?)?\\(?([0-9]{3})\\)?[-.]?([0-9]{3})[-.]?([0-9]{4})\\b"
    );
    private static final Pattern SSN_PATTERN = Pattern.compile(
        "\\b\\d{3}-?\\d{2}-?\\d{4}\\b"
    );

    /**
     * Process chat message through the complete pipeline
     * @param userId the user ID
     * @param message the user message
     * @return chat response with answer and sources
     */
    public ChatController.ChatResponse processMessage(Long userId, String message) {
        try {
            // Rate limiting check
            if (!checkRateLimit(userId)) {
                return new ChatController.ChatResponse(
                    "Rate limit exceeded. Please wait before sending another message.",
                    null, null
                );
            }

            // Step 1: Sanitize input
            SanitizationResult sanitizationResult = sanitizeInput(message);
            if (sanitizationResult.hasPii()) {
                // REVIEW: production logging/privacy rules - log PII detection without storing actual PII
                logger.warn("PII detected in message from user {}: {}", userId, sanitizationResult.getDetectedPiiTypes());
                return new ChatController.ChatResponse(
                    "I can't process PII — please remove personal information.",
                    null, sanitizationResult.getSanitizedMessage()
                );
            }

            // Step 2: Retrieve relevant sources
            List<String> sources = retrieveSources(sanitizationResult.getSanitizedMessage());

            // Step 3: Build prompt
            String prompt = buildPrompt(sanitizationResult.getSanitizedMessage(), sources);

            // Step 4: Call LLM
            String answer = callLLM(prompt);

            // Step 5: Log chat
            logChat(userId, message, answer, sources);

            return new ChatController.ChatResponse(answer, sources, sanitizationResult.getSanitizedMessage());

        } catch (Exception e) {
            logger.error("Error processing chat message for user {}: {}", userId, e.getMessage(), e);
            return new ChatController.ChatResponse(
                "I'm sorry, I encountered an error while processing your message. Please try again.",
                null, null
            );
        }
    }

    /**
     * Sanitize input and detect PII
     * @param input the input message
     * @return sanitization result
     */
    public SanitizationResult sanitizeInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new SanitizationResult(input, false, Collections.emptyList());
        }

        String sanitized = input;
        List<String> detectedPiiTypes = new ArrayList<>();
        boolean hasPii = false;

        // Check for email addresses
        if (EMAIL_PATTERN.matcher(input).find()) {
            detectedPiiTypes.add("EMAIL");
            hasPii = true;
            sanitized = EMAIL_PATTERN.matcher(sanitized).replaceAll("[EMAIL_REDACTED]");
        }

        // Check for phone numbers
        if (PHONE_PATTERN.matcher(input).find()) {
            detectedPiiTypes.add("PHONE");
            hasPii = true;
            sanitized = PHONE_PATTERN.matcher(sanitized).replaceAll("[PHONE_REDACTED]");
        }

        // Check for SSN
        if (SSN_PATTERN.matcher(input).find()) {
            detectedPiiTypes.add("SSN");
            hasPii = true;
            sanitized = SSN_PATTERN.matcher(sanitized).replaceAll("[SSN_REDACTED]");
        }

        // REVIEW: production logging/privacy rules - log sanitization without storing original content
        logger.debug("Input sanitized. PII detected: {}, Types: {}", hasPii, detectedPiiTypes);

        return new SanitizationResult(sanitized, hasPii, detectedPiiTypes);
    }

    /**
     * Retrieve relevant sources using keyword search and optionally vector search
     * @param query the sanitized query
     * @return list of relevant sources
     */
    private List<String> retrieveSources(String query) {
        List<String> sources = new ArrayList<>();

        // Keyword search in knowledge repository
        List<KnowledgeDoc> keywordResults = knowledgeRepository.searchByKeywords(query, topK);
        sources.addAll(keywordResults.stream()
                .map(KnowledgeDoc::getContent)
                .toList());

        // Optional vector search if configured
        if (vectorDbClient.isConfigured()) {
            try {
                List<String> vectorResults = vectorDbClient.searchSimilar(query, topK);
                sources.addAll(vectorResults);
            } catch (Exception e) {
                logger.warn("Vector search failed: {}", e.getMessage());
            }
        }

        // Remove duplicates and limit results
        return sources.stream()
                .distinct()
                .limit(topK)
                .toList();
    }

    /**
     * Build prompt with system message and context
     * @param query the sanitized query
     * @param sources the retrieved sources
     * @return built prompt
     */
    public String buildPrompt(String query, List<String> sources) {
        StringBuilder prompt = new StringBuilder();

        // System message (MUST be prepended as per requirements)
        prompt.append("System: \"You are an educational finance assistant. THIS IS NOT FINANCIAL ADVICE. ");
        prompt.append("If user asks for personalised recommendations, respond with 'Consult a licensed financial advisor' ");
        prompt.append("and provide educational resources.\"\n\n");

        // Add context from sources
        if (!sources.isEmpty()) {
            prompt.append("Context from knowledge base:\n");
            for (int i = 0; i < sources.size(); i++) {
                prompt.append(String.format("%d. %s\n", i + 1, sources.get(i)));
            }
            prompt.append("\n");
        }

        // Add user query
        prompt.append("User question: ").append(query);

        return prompt.toString();
    }

    /**
     * Call OpenAI LLM
     * @param prompt the built prompt
     * @return LLM response
     */
    private String callLLM(String prompt) {
        try {
            // TODO: Implement actual OpenAI API call
            // This is a placeholder implementation
            // In production, you would:
            // 1. Create OpenAI client with API key
            // 2. Send chat completion request
            // 3. Handle response and errors
            // 4. Implement retry logic

            logger.debug("Calling LLM with prompt length: {}", prompt.length());

            // Simulate LLM response for development
            return "This is a simulated response from the LLM. In production, this would be replaced with actual OpenAI API call.";

        } catch (Exception e) {
            logger.error("Error calling LLM: {}", e.getMessage(), e);
            return "I'm sorry, I'm having trouble processing your request right now. Please try again later.";
        }
    }

    /**
     * Check rate limit for user
     * @param userId the user ID
     * @return true if within rate limit, false otherwise
     */
    private boolean checkRateLimit(Long userId) {
        long currentTime = System.currentTimeMillis();
        long oneMinuteAgo = currentTime - 60000; // 1 minute in milliseconds

        List<Long> requestTimes = userRequestTimes.computeIfAbsent(userId, k -> new ArrayList<>());

        // Remove old requests (older than 1 minute)
        requestTimes.removeIf(time -> time < oneMinuteAgo);

        // Check if under rate limit
        if (requestTimes.size() >= rateLimitPerMinute) {
            return false;
        }

        // Add current request
        requestTimes.add(currentTime);
        return true;
    }

    /**
     * Log chat interaction
     * @param userId the user ID
     * @param query the original query
     * @param answer the LLM answer
     * @param sources the sources used
     */
    private void logChat(Long userId, String query, String answer, List<String> sources) {
        try {
            ChatLog chatLog = new ChatLog();
            chatLog.setUserId(userId);
            chatLog.setQuery(query);
            chatLog.setAnswer(answer);
            chatLog.setSources(String.join(";", sources));
            chatLog.setTimestamp(LocalDateTime.now());

            chatLogRepository.save(chatLog);

            // REVIEW: production logging/privacy rules - ensure sensitive data is not logged
            logger.debug("Chat logged for user: {}, query length: {}, answer length: {}", 
                        userId, query.length(), answer.length());

        } catch (Exception e) {
            logger.error("Error logging chat for user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Get chat history for user
     * @param userId the user ID
     * @return list of chat logs
     */
    public List<ChatLog> getChatHistory(Long userId) {
        return chatLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    /**
     * Get chat statistics for user
     * @param userId the user ID
     * @return chat statistics
     */
    public ChatStats getChatStats(Long userId) {
        List<ChatLog> chatLogs = chatLogRepository.findByUserIdOrderByTimestampDesc(userId);
        
        return new ChatStats(
            userId,
            chatLogs.size(),
            chatLogs.stream().mapToInt(log -> log.getQuery().length()).sum(),
            chatLogs.stream().mapToInt(log -> log.getAnswer().length()).sum(),
            LocalDateTime.now()
        );
    }

    /**
     * Sanitization result model
     */
    public static class SanitizationResult {
        private final String sanitizedMessage;
        private final boolean hasPii;
        private final List<String> detectedPiiTypes;

        public SanitizationResult(String sanitizedMessage, boolean hasPii, List<String> detectedPiiTypes) {
            this.sanitizedMessage = sanitizedMessage;
            this.hasPii = hasPii;
            this.detectedPiiTypes = detectedPiiTypes;
        }

        public String getSanitizedMessage() { return sanitizedMessage; }
        public boolean hasPii() { return hasPii; }
        public List<String> getDetectedPiiTypes() { return detectedPiiTypes; }
    }

    /**
     * Chat statistics model
     */
    public static class ChatStats {
        private Long userId;
        private int totalMessages;
        private int totalQueryLength;
        private int totalAnswerLength;
        private LocalDateTime lastUpdated;

        public ChatStats(Long userId, int totalMessages, int totalQueryLength, int totalAnswerLength, LocalDateTime lastUpdated) {
            this.userId = userId;
            this.totalMessages = totalMessages;
            this.totalQueryLength = totalQueryLength;
            this.totalAnswerLength = totalAnswerLength;
            this.lastUpdated = lastUpdated;
        }

        // Getters
        public Long getUserId() { return userId; }
        public int getTotalMessages() { return totalMessages; }
        public int getTotalQueryLength() { return totalQueryLength; }
        public int getTotalAnswerLength() { return totalAnswerLength; }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
    }
}
