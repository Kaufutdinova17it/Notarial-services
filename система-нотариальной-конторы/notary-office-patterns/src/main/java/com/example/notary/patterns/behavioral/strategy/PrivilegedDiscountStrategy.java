package com.example.notary.patterns.behavioral.strategy;

import com.example.notary.model.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Паттерн «Стратегия» — льготный тариф со скидкой от базовой цены.
 * Скидка по умолчанию — 30 % (льготные категории клиентов).
 */
public class PrivilegedDiscountStrategy implements PricingStrategy {

    /** Скидка по умолчанию: 30 %. */
    public static final BigDecimal DEFAULT_DISCOUNT = new BigDecimal("0.30");

    private final BigDecimal defaultDiscount;

    public PrivilegedDiscountStrategy() {
        this(DEFAULT_DISCOUNT);
    }

    public PrivilegedDiscountStrategy(BigDecimal defaultDiscount) {
        this.defaultDiscount = defaultDiscount;
    }

    @Override
    public BigDecimal calculate(Service service, PricingContext context) {
        BigDecimal discount = context.getDiscountRate() != null ? context.getDiscountRate() : defaultDiscount;
        return service.getPrice()
                .multiply(BigDecimal.ONE.subtract(discount))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getDescription() {
        return "льготный тариф со скидкой %s%%".formatted(
                defaultDiscount.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString());
    }
}
