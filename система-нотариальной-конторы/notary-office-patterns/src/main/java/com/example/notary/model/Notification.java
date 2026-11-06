package com.example.notary.model;

import java.time.LocalDateTime;

/**
 * Сущность «Уведомление» клиенту о событии заявки.
 * Текст формируется конкретной фабрикой (паттерн «Фабричный метод»).
 */
public class Notification {

    private Long id;
    private Long clientId;
    private Long applicationId;
    private NotificationType type;
    private String message;
    private NotificationStatus status;
    private LocalDateTime createdAt;

    public Notification() {
    }

    public Notification(Long id, Long clientId, Long applicationId, NotificationType type,
                        String message, NotificationStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.clientId = clientId;
        this.applicationId = applicationId;
        this.type = type;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
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
        return "Уведомление #%d [%s]: %s (статус «%s»)".formatted(
                id, type.getDisplayName(), message, status.getDisplayName());
    }
}
