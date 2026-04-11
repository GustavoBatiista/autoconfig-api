package com.gustavobatista.autoconfig.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.gustavobatista.autoconfig.enums.OrderStatus;

public class OrderResponseDTO {

    private final Long id;
    private final LocalDateTime orderDate;
    /** Persisted creation timestamp from {@link com.gustavobatista.autoconfig.entity.Auditable}. */
    private final LocalDateTime createdAt;
    private final BigDecimal totalPrice;
    private final OrderStatus status;
    private final ClientResponseDTO client;
    private final CarResponseDTO car;
    private final List<AccessoryResponseDTO> accessories;

    public OrderResponseDTO(Long id, LocalDateTime orderDate, LocalDateTime createdAt, BigDecimal totalPrice,
            OrderStatus status,
            ClientResponseDTO client, CarResponseDTO car, List<AccessoryResponseDTO> accessories) {
        this.id = id;
        this.orderDate = orderDate;
        this.createdAt = createdAt;
        this.totalPrice = totalPrice;
        this.status = status;
        this.client = client;
        this.car = car;
        this.accessories = accessories;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public ClientResponseDTO getClient() {
        return client;
    }

    public CarResponseDTO getCar() {
        return car;
    }

    public List<AccessoryResponseDTO> getAccessories() {
        return accessories;
    }

}
