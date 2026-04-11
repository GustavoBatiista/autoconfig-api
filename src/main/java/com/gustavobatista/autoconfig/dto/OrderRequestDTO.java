package com.gustavobatista.autoconfig.dto;

import java.util.List;

import com.gustavobatista.autoconfig.enums.OrderStatus;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class OrderRequestDTO {

    @NotNull(message = "Client id is required")
    private final Long clientId;
    @NotNull(message = "Car id is required")
    private final Long carId;
    @NotEmpty(message = "At least one accessory is required")
    private final List<Long> accessoryIds;
    @NotNull(message = "Status is required")
    private final OrderStatus status;

    public OrderRequestDTO(Long clientId, Long carId, List<Long> accessoryIds, OrderStatus status) {
        this.clientId = clientId;
        this.carId = carId;
        this.accessoryIds = accessoryIds == null ? List.of() : List.copyOf(accessoryIds);
        this.status = status;
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

    public OrderStatus getStatus() {
        return status;
    }

}
