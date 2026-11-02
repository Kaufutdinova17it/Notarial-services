package com.example.notary.model;

/** Тип тарификации нотариальной услуги. */
public enum PricingType {
    FIXED("Фиксированный тариф"),
    PERCENTAGE("Процент от суммы сделки");

    private final String title;

    PricingType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
