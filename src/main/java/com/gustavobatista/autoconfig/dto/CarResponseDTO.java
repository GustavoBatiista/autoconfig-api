package com.gustavobatista.autoconfig.dto;

public class CarResponseDTO {

    private final Long id;
    private final String brand;
    private final String model;
    private final String version;

    public CarResponseDTO(Long id, String brand, String model, String version) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getVersion() {
        return version;
    }
}
