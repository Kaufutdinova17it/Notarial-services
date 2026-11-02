package com.example.notary.service;

import com.example.notary.model.NotaryService;
import com.example.notary.model.PricingType;
import com.example.notary.patterns.behavioral.strategy.FixedPricingStrategy;
import com.example.notary.patterns.behavioral.strategy.PercentagePricingStrategy;
import com.example.notary.patterns.behavioral.strategy.PricingStrategy;
import com.example.notary.patterns.behavioral.strategy.PrivilegedDiscountStrategy;
import com.example.notary.repository.ServiceRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Сервис каталога нотариальных услуг: перечень услуг, тарифы. */
public class ServiceCatalogService {

    /** Ставка процентного тарифа — 0,5% от суммы сделки. */
    private static final BigDecimal DEAL_RATE = new BigDecimal("0.005");
    /** Скидка для льготной категории клиентов — 30%. */
    private static final BigDecimal PRIVILEGED_DISCOUNT = new BigDecimal("0.30");

    private final ServiceRepository serviceRepository;

    public ServiceCatalogService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<NotaryService> getAll() {
        return serviceRepository.findAll();
    }

    public NotaryService getById(UUID id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Услуга не найдена: " + id));
    }

    public NotaryService addService(NotaryService service) {
        return serviceRepository.save(service);
    }

    /**
     * Расчёт стоимости услуги. Стратегия выбирается по типу тарифа услуги
     * и категории клиента (паттерн «Стратегия»).
     */
    public BigDecimal calculatePrice(UUID serviceId, BigDecimal dealAmount, boolean privilegedClient) {
        NotaryService service = getById(serviceId);
        PricingStrategy strategy = resolveStrategy(service, privilegedClient);
        BigDecimal price = strategy.calculate(service, dealAmount);
        System.out.println("   Тарификация: " + strategy.getDescription() + " -> " + price + " руб.");
        return price;
    }

    private PricingStrategy resolveStrategy(NotaryService service, boolean privilegedClient) {
        if (service.getPricingType() == PricingType.PERCENTAGE) {
            return new PercentagePricingStrategy(DEAL_RATE);
        }
        if (privilegedClient) {
            return new PrivilegedDiscountStrategy(PRIVILEGED_DISCOUNT);
        }
        return new FixedPricingStrategy();
    }

    /** Наполнение каталога услугами по умолчанию (при первом запуске). */
    public void seedDefaultServices() {
        if (!serviceRepository.findAll().isEmpty()) {
            return;
        }
        addService(service("Оформление доверенности",
                "Подготовка и удостоверение доверенности", "3500", PricingType.FIXED));
        addService(service("Заверение копии документа",
                "Засвидетельствование верности копии документа", "2000", PricingType.FIXED));
        addService(service("Оформление согласия супруга",
                "Удостоверение согласия супруга на сделку", "2500", PricingType.FIXED));
        addService(service("Удостоверение договора купли-продажи недвижимости",
                "Удостоверение договора, тариф — 0,5% от суммы сделки (минимум 5000 руб.)",
                "5000", PricingType.PERCENTAGE));
    }

    private NotaryService service(String name, String description, String price, PricingType type) {
        NotaryService s = new NotaryService();
        s.setName(name);
        s.setDescription(description);
        s.setPrice(new BigDecimal(price));
        s.setPricingType(type);
        return s;
    }
}
