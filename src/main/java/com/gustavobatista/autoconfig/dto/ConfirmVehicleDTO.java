package com.gustavobatista.autoconfig.dto;

import java.time.LocalDateTime;

import com.gustavobatista.autoconfig.enums.VehicleCondition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ConfirmVehicleDTO {

    @NotBlank
    @Size(min = 17, max = 17)
    private final String chassis;

    @NotNull
    private final LocalDateTime arrivalDate;

    @NotNull
    private final VehicleCondition condition;

    public ConfirmVehicleDTO(String chassis, LocalDateTime arrivalDate, VehicleCondition condition) {
        this.chassis = chassis;
        this.arrivalDate = arrivalDate;
        this.condition = condition;
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
}
