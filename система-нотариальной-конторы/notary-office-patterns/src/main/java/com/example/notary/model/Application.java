package com.example.notary.model;

import java.time.LocalDateTime;

/**
 * Сущность «Заявка» клиента на нотариальную услугу.
 * Создаётся через паттерн «Строитель» со статусом «Создана».
 */
public class Application {

    private Long id;
    private Long clientId;
    private Long serviceId;
    private ApplicationStatus status;
    private String note;
    private LocalDateTime createdAt;

    public Application() {
    }

    public Application(Long id, Long clientId, Long serviceId, ApplicationStatus status,
                       String note, LocalDateTime createdAt) {
        this.id = id;
        this.clientId = clientId;
        this.serviceId = serviceId;
        this.status = status;
        this.note = note;
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

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Заявка #%d: клиент #%d, услуга #%d, статус «%s»".formatted(
                id, clientId, serviceId, status.getDisplayName());
    }
}
