package com.example.notary.model;

/**
 * Статусы жизненного цикла заявки (модель данных из архитектурного плана).
 */
public enum ApplicationStatus {
    CREATED("Создана"),
    IN_PROGRESS("В процессе"),
    READY("Готова"),
    RECEIVED("Получена"),
    REJECTED("Отклонена"),
    CANCELLED("Отменена");

    private final String displayName;

    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
