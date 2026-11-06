package com.example.notary.service;

import com.example.notary.model.PricingType;
import com.example.notary.model.Service;
import com.example.notary.repository.ServiceRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Сервис каталога нотариальных услуг.
 */
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;

    public ServiceCatalogService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    /** Заполняет каталог услуг по умолчанию при первом запуске. */
    public void ensureDefaultServices() {
        if (!serviceRepository.findAll().isEmpty()) {
            return;
        }
        serviceRepository.save(new Service(null,
                "Удостоверение доверенности",
                "Удостоверение доверенности на совершение сделок, получение документов и иных действий.",
                new BigDecimal("1500.00"), PricingType.FIXED));
        serviceRepository.save(new Service(null,
                "Свидетельствование верности копии документа",
                "Нотариальное засвидетельствование копии документа с подлинником.",
                new BigDecimal("500.00"), PricingType.FIXED));
        serviceRepository.save(new Service(null,
                "Удостоверение завещания",
                "Удостоверение завещания. Льготным категориям клиентов — скидка 30 %.",
                new BigDecimal("2000.00"), PricingType.PRIVILEGED));
        serviceRepository.save(new Service(null,
                "Удостоверение договора купли-продажи недвижимости",
                "Тариф — 0,5 % от суммы сделки.",
                BigDecimal.ZERO, PricingType.PERCENTAGE));
    }

    public List<Service> listServices() {
        return serviceRepository.findAll();
    }

    public Service create(String name, String description, BigDecimal price, PricingType pricingType) {
        validate(name, price, pricingType);
        return serviceRepository.save(new Service(null, name.trim(),
                description == null ? "" : description.trim(), price, pricingType));
    }

    public Service update(long id, String name, String description, BigDecimal price, PricingType pricingType) {
        Service service = findById(id);
        validate(name, price, pricingType);
        service.setName(name.trim());
        service.setDescription(description == null ? "" : description.trim());
        service.setPrice(price);
        service.setPricingType(pricingType);
        return serviceRepository.update(service);
    }

    public void delete(long id) {
        findById(id);
        serviceRepository.delete(id);
    }

    private void validate(String name, BigDecimal price, PricingType pricingType) {
        if (name == null || name.isBlank() || price == null || price.signum() < 0 || pricingType == null) {
            throw new IllegalArgumentException("Укажите название, неотрицательный тариф и тип тарификации");
        }
    }

    public Service findById(long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Услуга #" + id + " не найдена"));
    }
}
