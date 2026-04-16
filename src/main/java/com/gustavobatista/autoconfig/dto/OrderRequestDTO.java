package com.gustavobatista.autoconfig.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class OrderRequestDTO {

    @NotNull(message = "Client id is required")
    private final Long clientId;
    @NotNull(message = "Car id is required")
    private final Long carId;
    @NotEmpty(message = "At least one accessory is required")
    private final List<Long> accessoryIds;

    public OrderRequestDTO(Long clientId, Long carId, List<Long> accessoryIds) {
        this.clientId = clientId;
        this.carId = carId;
        this.accessoryIds = accessoryIds == null ? List.of() : List.copyOf(accessoryIds);
    }

    public Long getClientId() {
        return clientId;
    }

    public Long getCarId() {
        return carId;
    }

    public List<Long> getAccessoryIds() {
        return accessoryIds;
    }
}
