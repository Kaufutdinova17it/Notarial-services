package com.example.notary.model;

import java.time.LocalDateTime;
import java.util.UUID;

/** Запись журнала аудита (протоколирование действий пользователя). */
public class AuditRecord {
    private UUID id = UUID.randomUUID();
    private String actor;
    private String action;
    private String details;
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return createdAt + " | " + actor + " | " + action + " | " + details;
    }
}
