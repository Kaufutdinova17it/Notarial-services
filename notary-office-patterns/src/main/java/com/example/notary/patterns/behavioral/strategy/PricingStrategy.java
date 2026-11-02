package com.example.notary.patterns.behavioral.strategy;

import com.example.notary.model.NotaryService;

import java.math.BigDecimal;

/**
 * ПОВЕДЕНЧЕСКИЙ ПАТТЕРН — СТРАТЕГИЯ (Strategy), общий интерфейс.
 *
 * Задача из предметной области: стоимость нотариальной услуги
 * рассчитывается по-разному — фиксированный тариф, процент от суммы
 * сделки, льготный тариф со скидкой. Алгоритм расчёта выбирается
 * во время выполнения и легко заменяется.
 */
public interface PricingStrategy {

    /**
     * Рассчитывает итоговую стоимость услуги.
     *
     * @param service    услуга из каталога
     * @param dealAmount сумма сделки (используется процентным тарифом, может быть null)
     */
    BigDecimal calculate(NotaryService service, BigDecimal dealAmount);

    String getDescription();
}
