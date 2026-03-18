package com.gustavobatista.autoconfig.dto;

import java.time.LocalDateTime;

import com.gustavobatista.autoconfig.enums.VehicleCondition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VehicleEntryRequestDTO {

    @NotBlank(message = "Chassis is required")
    @Size(min = 17, max = 17)
    private final String chassis;

    @NotNull(message = "Arrival date is required")
    private final LocalDateTime arrivalDate;

    @NotNull(message = "Vehicle condition is required")
    private final VehicleCondition condition;

    @NotNull(message = "Order id is required")
    private final Long orderId;

    public VehicleEntryRequestDTO(String chassis, LocalDateTime arrivalDate, VehicleCondition condition, Long orderId) {
        this.chassis = chassis;
        this.arrivalDate = arrivalDate;
        this.condition = condition;
        this.orderId = orderId;
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
    
    public Long getOrderId() {
        return orderId;
    }
}
