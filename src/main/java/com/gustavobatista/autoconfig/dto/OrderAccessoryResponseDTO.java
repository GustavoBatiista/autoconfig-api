package com.gustavobatista.autoconfig.dto;

import java.math.BigDecimal;

import com.gustavobatista.autoconfig.entity.OrderAccessory;

public class OrderAccessoryResponseDTO {

    private final Long id;
    private final Long accessoryId;
    private final String name;
    private final BigDecimal price;

    public OrderAccessoryResponseDTO(Long id, Long accessoryId, String name, BigDecimal price) {
        this.id = id;
        this.accessoryId = accessoryId;
        this.name = name;
        this.price = price;
    }

    public static OrderAccessoryResponseDTO from(OrderAccessory line) {
        return new OrderAccessoryResponseDTO(
                line.getId(),
                line.getAccessory().getId(),
                line.getName(),
                line.getPrice());
    }

    public Long getId() {
        return id;
    }

    public Long getAccessoryId() {
        return accessoryId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
