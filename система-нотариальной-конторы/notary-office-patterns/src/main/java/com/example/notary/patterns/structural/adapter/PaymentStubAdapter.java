package com.example.notary.patterns.structural.adapter;

import java.math.BigDecimal;

/**
 * Паттерн «Адаптер» (Adapter) — согласует несовместимый интерфейс
 * {@link ExternalPaymentStub} (копейки, числовые коды) с интерфейсом
 * {@link PaymentGateway}, используемым системой (рубли, объект результата).
 */
public class PaymentStubAdapter implements PaymentGateway {

    private final ExternalPaymentStub externalPaymentStub;

    public PaymentStubAdapter(ExternalPaymentStub externalPaymentStub) {
        this.externalPaymentStub = externalPaymentStub;
    }

    @Override
    public PaymentResult pay(Long applicationId, BigDecimal amountRubles) {
        long amountKopecks = amountRubles.movePointRight(2).longValueExact();
        int resultCode = externalPaymentStub.makePayment(amountKopecks, "APP-" + applicationId);
        if (resultCode == ExternalPaymentStub.CODE_SUCCESS) {
            return PaymentResult.success(externalPaymentStub.lastTransactionId());
        }
        return PaymentResult.failure(externalPaymentStub.describeCode(resultCode));
    }
}
