package com.example.notary.patterns.behavioral.strategy;

import com.example.notary.model.NotaryService;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Процентный тариф: стоимость = процент от суммы сделки,
 * но не меньше минимальной стоимости услуги из каталога
 * (например, удостоверение договора купли-продажи недвижимости).
 */
public class PercentagePricingStrategy implements PricingStrategy {

    private final BigDecimal rate;

    public PercentagePricingStrategy(BigDecimal rate) {
        this.rate = rate;
    }

    @Override
    public BigDecimal calculate(NotaryService service, BigDecimal dealAmount) {
        if (dealAmount == null) {
            throw new IllegalArgumentException(
                    "Для услуги «" + service.getName() + "» необходимо указать сумму сделки");
        }
        BigDecimal percent = dealAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        return percent.max(service.getPrice());
    }

    @Override
    public String getDescription() {
        return "Процент от суммы сделки (" + rate.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString()
                + "%), минимум — базовая стоимость услуги";
    }
}
