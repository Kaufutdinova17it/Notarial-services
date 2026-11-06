package com.example.notary.model;

/**
 * Статусы платежа по заявке.
 */
public enum PaymentStatus {
    PENDING("Ожидает оплаты"),
    PAID("Оплачен"),
    FAILED("Ошибка оплаты");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
