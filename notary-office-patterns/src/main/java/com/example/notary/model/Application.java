package com.example.notary.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Заявка клиента на нотариальную услугу. */
public class Application {
    private UUID id = UUID.randomUUID();
    private UUID clientId;
    private UUID serviceId;
    private ApplicationStatus status = ApplicationStatus.CREATED;
    private String note;
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Документы заявки (хранятся в таблице documents). */
    private final List<Document> documents = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getClientId() { return clientId; }
    public void setClientId(UUID clientId) { this.clientId = clientId; }
    public UUID getServiceId() { return serviceId; }
    public void setServiceId(UUID serviceId) { this.serviceId = serviceId; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<Document> getDocuments() { return documents; }

    public String shortId() {
        return id.toString().substring(0, 8);
    }

    @Override
    public String toString() {
        return "Заявка №" + shortId() + " | статус: " + status.getTitle() + " | создана: " + createdAt;
    }
}
