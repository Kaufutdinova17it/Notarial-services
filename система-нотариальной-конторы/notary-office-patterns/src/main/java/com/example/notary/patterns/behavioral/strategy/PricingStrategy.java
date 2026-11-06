package com.example.notary.patterns.behavioral.strategy;

import com.example.notary.model.PricingType;
import com.example.notary.model.Service;

import java.math.BigDecimal;

/**
 * Паттерн «Стратегия» (Strategy) — алгоритм расчёта стоимости нотариальной
 * услуги. Конкретный алгоритм выбирается во время выполнения по типу
 * тарификации услуги.
 */
public interface PricingStrategy {

    /** Рассчитывает итоговую стоимость услуги. */
    BigDecimal calculate(Service service, PricingContext context);

    /** Краткое описание алгоритма для вывода пользователю. */
    String getDescription();

    /** Выбор стратегии во время выполнения по типу тарификации услуги. */
    static PricingStrategy forPricingType(PricingType type) {
        return switch (type) {
            case FIXED -> new FixedPricingStrategy();
            case PERCENTAGE -> new PercentagePricingStrategy();
            case PRIVILEGED -> new PrivilegedDiscountStrategy();
        };
    }
}
