package com.gustavobatista.autoconfig.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CarRequestDTO {

    @NotBlank(message = "Brand is required")
    @Size(min = 3, max = 50)
    private final String brand;

    @NotBlank(message = "Model is required")
    @Size(min = 3, max = 50)
    private final String model;

    @NotBlank(message = "Version is required")
    @Size(min = 3, max = 50)
    private final String version;

    public CarRequestDTO(String brand, String model, String version) {
        this.brand = brand;
        this.model = model;
        this.version = version;
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
