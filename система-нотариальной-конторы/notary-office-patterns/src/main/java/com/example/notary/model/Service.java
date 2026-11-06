package com.example.notary.model;

import java.math.BigDecimal;

/**
 * Сущность «Нотариальная услуга» — позиция каталога услуг конторы.
 * Поле {@link PricingType} определяет стратегию расчёта итоговой стоимости.
 */
public class Service {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private PricingType pricingType;

    public Service() {
    }

    public Service(Long id, String name, String description, BigDecimal price, PricingType pricingType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.pricingType = pricingType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public PricingType getPricingType() {
        return pricingType;
    }

    public void setPricingType(PricingType pricingType) {
        this.pricingType = pricingType;
    }

    @Override
    public String toString() {
        return "Услуга #%d: %s — %.2f руб. (%s)".formatted(id, name, price, pricingType.getDisplayName());
    }
}
