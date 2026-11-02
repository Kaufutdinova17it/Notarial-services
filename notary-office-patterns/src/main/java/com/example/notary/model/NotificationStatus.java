package com.example.notary.model;

/** Статус отправки уведомления. */
public enum NotificationStatus {
    CREATED("Создано"),
    SENT("Отправлено"),
    ERROR("Ошибка");

    private final String title;

    NotificationStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
