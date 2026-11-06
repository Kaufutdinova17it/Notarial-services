package com.example.notary.patterns.behavioral.strategy;

import java.math.BigDecimal;

/**
 * Контекст расчёта стоимости для паттерна «Стратегия»:
 * сумма сделки (для процентного тарифа) и ставка скидки (для льготного).
 */
public class PricingContext {

    /** Контекст по умолчанию — без суммы сделки и без переопределения скидки. */
    public static final PricingContext EMPTY = new PricingContext(null, null);

    private final BigDecimal dealAmount;
    private final BigDecimal discountRate;

    private PricingContext(BigDecimal dealAmount, BigDecimal discountRate) {
        this.dealAmount = dealAmount;
        this.discountRate = discountRate;
    }

    public static PricingContext ofDealAmount(BigDecimal dealAmount) {
        return new PricingContext(dealAmount, null);
    }

    public static PricingContext ofDiscountRate(BigDecimal discountRate) {
        return new PricingContext(null, discountRate);
    }

    /** Сумма сделки; {@code null}, если не задана. */
    public BigDecimal getDealAmount() {
        return dealAmount;
    }

    /** Явно заданная ставка скидки; {@code null} — использовать ставку стратегии по умолчанию. */
    public BigDecimal getDiscountRate() {
        return discountRate;
    }
}
