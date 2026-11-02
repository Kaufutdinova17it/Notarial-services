package com.example.notary.model;

import java.time.LocalDateTime;
import java.util.UUID;

/** Уведомление клиента о событии по заявке. */
public class Notification {
    private UUID id = UUID.randomUUID();
    private UUID clientId;
    private UUID applicationId;
    private NotificationType type;
    private String message;
    private NotificationStatus status = NotificationStatus.CREATED;
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getClientId() { return clientId; }
    public void setClientId(UUID clientId) { this.clientId = clientId; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "[" + type.getTitle() + " -> " + status.getTitle() + "] " + message;
    }
}
