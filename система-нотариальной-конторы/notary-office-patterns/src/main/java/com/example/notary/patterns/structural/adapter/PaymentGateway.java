package com.example.notary.patterns.structural.adapter;

import java.math.BigDecimal;

/**
 * Целевой интерфейс платёжного шлюза, используемый системой.
 * Паттерн «Адаптер» ({@link PaymentStubAdapter}) согласует с ним
 * несовместимый интерфейс внешнего платёжного сервиса.
 */
public interface PaymentGateway {

    /**
     * Проводит оплату по заявке.
     *
     * @param applicationId номер заявки
     * @param amountRubles  сумма в рублях
     */
    PaymentResult pay(Long applicationId, BigDecimal amountRubles);
}
