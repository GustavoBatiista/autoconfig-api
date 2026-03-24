package com.gustavobatista.autoconfig.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AccessoryRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50)
    private final String name;

    @NotBlank(message = "Description is required")
    @Size(min = 3, max = 50)
    private final String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private final BigDecimal price;

    @NotNull(message = "Car id is required")
    private final Long carId;

    public AccessoryRequestDTO(String name, String description, BigDecimal price, Long carId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.carId = carId;
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

    public Long getCarId() {
        return carId;
    }
}
