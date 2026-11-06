package com.example.notary.patterns.behavioral.observer;

import java.time.LocalDateTime;

/**
 * Событие заявки, публикуемое издателем {@link ApplicationEventPublisher}.
 */
public class ApplicationEvent {

    private final ApplicationEventType type;
    private final Long applicationId;
    private final Long clientId;
    private final String details;
    private final LocalDateTime occurredAt;

    public ApplicationEvent(ApplicationEventType type, Long applicationId, Long clientId, String details) {
        this.type = type;
        this.applicationId = applicationId;
        this.clientId = clientId;
        this.details = details;
        this.occurredAt = LocalDateTime.now();
    }

    public ApplicationEventType getType() {
        return type;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
