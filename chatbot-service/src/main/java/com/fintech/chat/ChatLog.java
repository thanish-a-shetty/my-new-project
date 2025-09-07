package com.fintech.chat;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_logs")
public class ChatLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String query;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String answer;

    @Column(columnDefinition = "TEXT")
    private String sources;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    // Constructors
    public ChatLog() {}

    public ChatLog(Long userId, String query, String answer, String sources) {
        this.userId = userId;
        this.query = query;
        this.answer = answer;
        this.sources = sources;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getSources() {
        return sources;
    }

    public void setSources(String sources) {
        this.sources = sources;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public Integer getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "ChatLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", query='" + query + '\'' +
                ", answer='" + answer + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
