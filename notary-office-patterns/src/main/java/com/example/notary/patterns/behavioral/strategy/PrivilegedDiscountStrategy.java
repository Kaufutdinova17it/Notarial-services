package com.example.notary.patterns.behavioral.strategy;

import com.example.notary.model.NotaryService;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Льготный тариф: скидка от фиксированной стоимости для льготной категории клиентов. */
public class PrivilegedDiscountStrategy implements PricingStrategy {

    private final BigDecimal discount;

    public PrivilegedDiscountStrategy(BigDecimal discount) {
        this.discount = discount;
    }

    @Override
    public BigDecimal calculate(NotaryService service, BigDecimal dealAmount) {
        return service.getPrice()
                .multiply(BigDecimal.ONE.subtract(discount))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getDescription() {
        return "Льготный тариф (скидка " + discount.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() + "%)";
    }
}
