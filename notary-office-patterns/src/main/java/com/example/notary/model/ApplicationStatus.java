package com.example.notary.model;

/** Статусы заявки (справочник «Статусы заявок»). */
public enum ApplicationStatus {
    CREATED("Создана"),
    IN_PROGRESS("В процессе"),
    READY("Готова"),
    RECEIVED("Получена"),
    REJECTED("Отклонена"),
    CANCELLED("Отменена");

    private final String title;

    ApplicationStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
