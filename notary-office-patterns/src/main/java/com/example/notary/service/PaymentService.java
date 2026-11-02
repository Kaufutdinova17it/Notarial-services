package com.example.notary.service;

import com.example.notary.model.Payment;
import com.example.notary.model.PaymentStatus;
import com.example.notary.patterns.structural.adapter.PaymentGateway;
import com.example.notary.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/** Сервис оплаты: создаёт платёж и проводит его через платёжный шлюз (адаптер заглушки). */
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(PaymentRepository paymentRepository, PaymentGateway paymentGateway) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
    }

    /** Создание платежа по заявке и имитация оплаты. */
    public Payment createPayment(UUID applicationId, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setApplicationId(applicationId);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.CREATED);
        paymentRepository.save(payment);

        boolean success = paymentGateway.pay(applicationId, amount);
        payment.setStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        paymentRepository.updateStatus(payment.getId(), payment.getStatus());
        return payment;
    }

    public Payment getById(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Платёж не найден: " + id));
    }

    public Optional<Payment> findByApplication(UUID applicationId) {
        return paymentRepository.findByApplicationId(applicationId);
    }
}
