package com.example.notary.patterns.behavioral.strategy;

import com.example.notary.model.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Паттерн «Стратегия» — фиксированный тариф:
 * стоимость равна базовой цене услуги из каталога.
 */
public class FixedPricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal calculate(Service service, PricingContext context) {
        return service.getPrice().setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getDescription() {
        return "фиксированный тариф";
    }
}
