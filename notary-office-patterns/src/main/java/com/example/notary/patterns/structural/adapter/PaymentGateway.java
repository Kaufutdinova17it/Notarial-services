package com.example.notary.patterns.structural.adapter;

import java.math.BigDecimal;
import java.util.UUID;

/** Целевой интерфейс платёжного шлюза, который использует наша система. */
public interface PaymentGateway {

    /**
     * Проводит оплату по заявке.
     *
     * @return true, если оплата прошла успешно
     */
    boolean pay(UUID applicationId, BigDecimal amount);
}
