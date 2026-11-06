package com.example.notary.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сущность «Платёж» по заявке. Сумма рассчитывается стратегией тарификации,
 * проведение выполняется через платёжный шлюз (адаптер внешней заглушки).
 */
public class Payment {

    private Long id;
    private Long applicationId;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime createdAt;

    public Payment() {
    }

    public Payment(Long id, Long applicationId, BigDecimal amount, PaymentStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Платёж #%d по заявке #%d: %.2f руб., статус «%s»".formatted(
                id, applicationId, amount, status.getDisplayName());
    }
}
