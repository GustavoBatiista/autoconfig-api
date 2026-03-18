package com.gustavobatista.autoconfig.dto;

import java.time.LocalDateTime;

import com.gustavobatista.autoconfig.enums.VehicleCondition;

public class VehicleEntryResponseDTO {

    private final Long id;
    private final String chassis;
    private final LocalDateTime arrivalDate;
    private final VehicleCondition condition;
    private final OrderResponseDTO order;

    public VehicleEntryResponseDTO(Long id, String chassis, LocalDateTime arrivalDate, VehicleCondition condition, OrderResponseDTO order) {
        this.id = id;
        this.chassis = chassis;
        this.arrivalDate = arrivalDate;
        this.condition = condition;
        this.order = order;
    }

    public Long getId() {
        return id;
    }

    public String getChassis() {
        return chassis;
    }

    public LocalDateTime getArrivalDate() {
        return arrivalDate;
    }

    public VehicleCondition getCondition() {
        return condition;
    }

    public OrderResponseDTO getOrder() {
        return order;
    }
}
