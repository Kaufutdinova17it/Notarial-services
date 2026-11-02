package com.example.notary.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Платёж по заявке (через заглушку платёжного сервиса). */
public class Payment {
    private UUID id = UUID.randomUUID();
    private UUID applicationId;
    private BigDecimal amount;
    private PaymentStatus status = PaymentStatus.CREATED;
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Платёж №" + id.toString().substring(0, 8) + " | сумма: " + amount
                + " руб. | статус: " + status.getTitle();
    }
}
