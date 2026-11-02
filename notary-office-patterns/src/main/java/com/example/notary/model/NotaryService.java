package com.example.notary.model;

import java.math.BigDecimal;
import java.util.UUID;

/** Нотариальная услуга (элемент каталога). */
public class NotaryService {
    private UUID id = UUID.randomUUID();
    private String name;
    private String description;
    /** Базовая стоимость; для процентного тарифа — минимальная стоимость. */
    private BigDecimal price;
    private PricingType pricingType = PricingType.FIXED;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public PricingType getPricingType() { return pricingType; }
    public void setPricingType(PricingType pricingType) { this.pricingType = pricingType; }

    @Override
    public String toString() {
        return name + " — " + price + " руб. (" + pricingType.getTitle() + ")";
    }
}
