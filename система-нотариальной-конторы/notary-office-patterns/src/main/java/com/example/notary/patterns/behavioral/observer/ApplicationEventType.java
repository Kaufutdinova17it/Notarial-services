package com.example.notary.patterns.behavioral.observer;

/**
 * Типы событий жизненного цикла заявки (событийное взаимодействие
 * из архитектурного плана).
 */
public enum ApplicationEventType {
    APPLICATION_CREATED,
    STATUS_CHANGED,
    DOCUMENTS_READY,
    APPLICATION_REJECTED,
    APPLICATION_CANCELLED
}
