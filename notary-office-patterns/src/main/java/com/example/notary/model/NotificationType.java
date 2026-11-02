package com.example.notary.model;

/** Типы уведомлений (справочник «Типы уведомлений»). */
public enum NotificationType {
    APPLICATION_CREATED("Создание заявки"),
    STATUS_CHANGED("Изменение статуса"),
    DOCUMENTS_READY("Готовность документов"),
    APPLICATION_REJECTED("Отклонение заявки"),
    APPLICATION_CANCELLED("Отмена заявки");

    private final String title;

    NotificationType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
