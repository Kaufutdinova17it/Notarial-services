package com.example.notary.model;

/**
 * Типы уведомлений — по одному на каждое событие заявки
 * (соответствуют конкретным фабрикам паттерна «Фабричный метод»).
 */
public enum NotificationType {
    APPLICATION_CREATED("Создание заявки"),
    STATUS_CHANGED("Смена статуса заявки"),
    DOCUMENTS_READY("Готовность документов"),
    APPLICATION_REJECTED("Отклонение заявки"),
    APPLICATION_CANCELLED("Отмена заявки");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
