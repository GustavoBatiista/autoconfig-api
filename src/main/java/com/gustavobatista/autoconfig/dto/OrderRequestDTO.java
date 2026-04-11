package com.gustavobatista.autoconfig.dto;

import java.math.BigDecimal;

import com.gustavobatista.autoconfig.enums.OrderStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class OrderRequestDTO {

    @NotNull(message = "Client id is required")
    private final Long clientId;
    @NotNull(message = "Car id is required")
    private final Long carId;
    @NotNull(message = "Accessory id is required")
    private final Long accessoryId;
    @NotNull(message = "Total price is required")
    @Positive(message = "Total price must be greater than 0")
    private final BigDecimal totalPrice;
    @NotNull(message = "Status is required")
    private final OrderStatus status;

    public OrderRequestDTO(Long clientId, Long carId, Long accessoryId, BigDecimal totalPrice, OrderStatus status) {
        this.clientId = clientId;
        this.carId = carId;
        this.accessoryId = accessoryId;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public Long getClientId() {
        return clientId;
    }

    public Long getCarId() {
        return carId;
    }

    public Long getAccessoryId() {
        return accessoryId;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

}
