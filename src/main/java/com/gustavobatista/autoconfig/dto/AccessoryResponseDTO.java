package com.gustavobatista.autoconfig.dto;

import java.math.BigDecimal;

public class AccessoryResponseDTO {


    private final Long id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final CarResponseDTO car;

    public AccessoryResponseDTO(Long id, String name, String description, BigDecimal price, CarResponseDTO car) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.car = car;
    }

    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }

    public CarResponseDTO getCar() {
        return car;
    }
}
