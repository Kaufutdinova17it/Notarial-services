package com.example.notary.model;

/**
 * Тип тарификации нотариальной услуги — определяет,
 * какая стратегия расчёта стоимости будет выбрана во время выполнения.
 */
public enum PricingType {
    FIXED("Фиксированный тариф"),
    PERCENTAGE("Процент от суммы сделки"),
    PRIVILEGED("Льготный тариф со скидкой");

    private final String displayName;

    PricingType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
