package com.gustavobatista.autoconfig.dto;

public class AccessoryResponseDTO {


    private final Long id;
    private final String name;
    private final String description;
    private final Double price;
    private final CarResponseDTO car;

    public AccessoryResponseDTO(Long id, String name, String description, Double price, CarResponseDTO car) {
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
    
    public Double getPrice() {
        return price;
    }

    public CarResponseDTO getCar() {
        return car;
    }
}
