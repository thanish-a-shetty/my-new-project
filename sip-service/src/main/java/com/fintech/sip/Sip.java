package com.fintech.sip;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sips")
public class Sip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SipFrequency frequency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SipStatus status = SipStatus.ACTIVE;

    @Column(name = "next_due_date", nullable = false)
    private LocalDateTime nextDueDate;

    @Column(name = "last_processed_at")
    private LocalDateTime lastProcessedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "description")
    private String description;

    // Constructors
    public Sip() {}

    public Sip(Long userId, String symbol, Double amount, SipFrequency frequency) {
        this.userId = userId;
        this.symbol = symbol;
        this.amount = amount;
        this.frequency = frequency;
        this.status = SipStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public SipFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(SipFrequency frequency) {
        this.frequency = frequency;
    }

    public SipStatus getStatus() {
        return status;
    }

    public void setStatus(SipStatus status) {
        this.status = status;
    }

    public LocalDateTime getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDateTime nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public LocalDateTime getLastProcessedAt() {
        return lastProcessedAt;
    }

    public void setLastProcessedAt(LocalDateTime lastProcessedAt) {
        this.lastProcessedAt = lastProcessedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Sip{" +
                "id=" + id +
                ", userId=" + userId +
                ", symbol='" + symbol + '\'' +
                ", amount=" + amount +
                ", frequency=" + frequency +
                ", status=" + status +
                ", nextDueDate=" + nextDueDate +
                ", lastProcessedAt=" + lastProcessedAt +
                ", createdAt=" + createdAt +
                '}';
    }
}
