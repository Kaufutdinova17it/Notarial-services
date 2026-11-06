package com.example.notary.service;

import com.example.notary.model.Application;
import com.example.notary.model.Payment;
import com.example.notary.model.PaymentStatus;
import com.example.notary.model.Service;
import com.example.notary.patterns.behavioral.strategy.PricingContext;
import com.example.notary.patterns.behavioral.strategy.PricingStrategy;
import com.example.notary.patterns.structural.adapter.PaymentGateway;
import com.example.notary.patterns.structural.adapter.PaymentResult;
import com.example.notary.repository.ApplicationRepository;
import com.example.notary.repository.PaymentRepository;
import com.example.notary.repository.ServiceRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис оплаты: стоимость рассчитывается выбранной стратегией тарификации,
 * проведение — через платёжный шлюз (адаптер внешней заглушки).
 */
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ApplicationRepository applicationRepository;
    private final ServiceRepository serviceRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(PaymentRepository paymentRepository,
                          ApplicationRepository applicationRepository,
                          ServiceRepository serviceRepository,
                          PaymentGateway paymentGateway) {
        this.paymentRepository = paymentRepository;
        this.applicationRepository = applicationRepository;
        this.serviceRepository = serviceRepository;
        this.paymentGateway = paymentGateway;
    }

    /**
     * Оплачивает заявку: стратегия выбирается во время выполнения
     * по типу тарификации услуги.
     */
    public Payment payForApplication(long applicationId, PricingContext context) {
        Application application = findApplication(applicationId);
        Service service = findService(application.getServiceId());
        PricingStrategy strategy = PricingStrategy.forPricingType(service.getPricingType());
        return payForApplication(applicationId, strategy, context);
    }

    /** Оплачивает заявку с явно указанной стратегией расчёта стоимости. */
    public Payment payForApplication(long applicationId, PricingStrategy strategy, PricingContext context) {
        Application application = findApplication(applicationId);
        Service service = findService(application.getServiceId());

        BigDecimal amount = strategy.calculate(service, context);
        PaymentResult result = paymentGateway.pay(applicationId, amount);

        Payment payment = new Payment(null, applicationId, amount,
                result.isSuccess() ? PaymentStatus.PAID : PaymentStatus.FAILED,
                LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);
        if (!result.isSuccess()) {
            throw new IllegalStateException("Платёж не прошёл: " + result.getMessage());
        }
        return saved;
    }

    public Payment findById(long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Платёж #" + id + " не найден"));
    }

    public List<Payment> findByApplicationId(long applicationId) {
        return paymentRepository.findByApplicationId(applicationId);
    }

    private Application findApplication(long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка #" + applicationId + " не найдена"));
    }

    private Service findService(long serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Услуга #" + serviceId + " не найдена"));
    }
}
