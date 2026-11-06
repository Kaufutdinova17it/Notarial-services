package com.example.notary.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Запись журнала аудита — протоколирование действий в системе
 * (заполняется слушателем событий {@code AuditLogListener}).
 */
public class AuditEntry {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private Long id;
    private String eventType;
    private String description;
    private LocalDateTime createdAt;

    public AuditEntry() {
    }

    public AuditEntry(Long id, String eventType, String description, LocalDateTime createdAt) {
        this.id = id;
        this.eventType = eventType;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "[%s] %s — %s".formatted(createdAt.format(FORMATTER), eventType, description);
    }
}
