package com.example.notary.model;

/** Статус платежа. */
public enum PaymentStatus {
    CREATED("Создан"),
    SUCCESS("Успешно"),
    FAILED("Неуспешно");

    private final String title;

    PaymentStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
