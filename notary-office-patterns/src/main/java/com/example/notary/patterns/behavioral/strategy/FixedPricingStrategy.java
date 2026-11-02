package com.example.notary.patterns.behavioral.strategy;

import com.example.notary.model.NotaryService;

import java.math.BigDecimal;

/** Фиксированный тариф: стоимость берётся из каталога услуг. */
public class FixedPricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal calculate(NotaryService service, BigDecimal dealAmount) {
        return service.getPrice();
    }

    @Override
    public String getDescription() {
        return "Фиксированный тариф";
    }
}
