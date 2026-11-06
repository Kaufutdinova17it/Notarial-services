package com.example.notary.model;

/**
 * Статусы отправки уведомления клиенту.
 */
public enum NotificationStatus {
    PENDING("Ожидает отправки"),
    SENT("Отправлено"),
    FAILED("Ошибка отправки");

    private final String displayName;

    NotificationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
