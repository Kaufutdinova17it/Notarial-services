package com.example.notary.patterns.behavioral.strategy;

import com.example.notary.model.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Паттерн «Стратегия» — процент от суммы сделки
 * (удостоверение договоров). Ставка по умолчанию — 0,5 %.
 */
public class PercentagePricingStrategy implements PricingStrategy {

    /** Ставка по умолчанию: 0,5 % от суммы сделки. */
    public static final BigDecimal DEFAULT_RATE = new BigDecimal("0.005");

    private final BigDecimal rate;

    public PercentagePricingStrategy() {
        this(DEFAULT_RATE);
    }

    public PercentagePricingStrategy(BigDecimal rate) {
        this.rate = rate;
    }

    @Override
    public BigDecimal calculate(Service service, PricingContext context) {
        BigDecimal dealAmount = context.getDealAmount();
        if (dealAmount == null || dealAmount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Для услуги «%s» требуется сумма сделки".formatted(service.getName()));
        }
        return dealAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getDescription() {
        return "процент от суммы сделки (%s%%)".formatted(
                rate.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString());
    }
}
