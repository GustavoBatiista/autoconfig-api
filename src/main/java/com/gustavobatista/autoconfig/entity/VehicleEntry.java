package com.gustavobatista.autoconfig.entity;

import java.time.LocalDateTime;

import com.gustavobatista.autoconfig.enums.VehicleCondition;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicle_entry")
public class VehicleEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "chassis", nullable = false)
    private String chassis;
    @Column(name = "arrival_date", nullable = false)
    private LocalDateTime arrivalDate;
    @Column(name = "vehicle_condition", nullable = false)
    @Enumerated(EnumType.STRING)
    private VehicleCondition condition;
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order orderId;

    public VehicleEntry() {
    }

    public VehicleEntry(Long id, String chassis, LocalDateTime arrivalDate, VehicleCondition condition, Order orderId) {
        this.id = id;
        this.chassis = chassis;
        this.arrivalDate = arrivalDate;
        this.condition = condition;
        this.orderId = orderId;
    }

    public Long getId() {
        return id;
    }

    public String getChassis() {
        return chassis;
    }

    public void setChassis(String chassis) {
        this.chassis = chassis;
    }

    public LocalDateTime getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDateTime arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public VehicleCondition getCondition() {
        return condition;
    }

    public void setCondition(VehicleCondition condition) {
        this.condition = condition;
    }

    public Order getOrderId() {
        return orderId;
    }

    public void setOrderId(Order orderId) {
        this.orderId = orderId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VehicleEntry other = (VehicleEntry) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "VehicleEntry [id=" + id + ", chassis=" + chassis + ", arrivalDate=" + arrivalDate + ", condition="
                + condition + ", orderId=" + orderId + "]";
    }

}
