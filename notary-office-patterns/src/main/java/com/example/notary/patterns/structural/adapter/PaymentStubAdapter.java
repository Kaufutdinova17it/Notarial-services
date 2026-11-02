package com.example.notary.patterns.structural.adapter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * СТРУКТУРНЫЙ ПАТТЕРН — АДАПТЕР (Adapter).
 *
 * Задача из предметной области: система работает с платежами через
 * интерфейс PaymentGateway (BigDecimal, UUID, boolean), а платёжная
 * заглушка имеет другой интерфейс (String, копейки, числовой код).
 * Адаптер преобразует вызовы одного интерфейса в другой.
 */
public class PaymentStubAdapter implements PaymentGateway {

    private final ExternalPaymentStub externalService;

    public PaymentStubAdapter(ExternalPaymentStub externalService) {
        this.externalService = externalService;
    }

    @Override
    public boolean pay(UUID applicationId, BigDecimal amount) {
        long amountInKopecks = amount.movePointRight(2).longValue();
        String orderReference = "APP-" + applicationId;
        int resultCode = externalService.makeTransaction(orderReference, amountInKopecks);
        return resultCode == ExternalPaymentStub.CODE_SUCCESS;
    }
}
