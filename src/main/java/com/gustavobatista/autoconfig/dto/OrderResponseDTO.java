package com.gustavobatista.autoconfig.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.gustavobatista.autoconfig.enums.OrderStatus;

public class OrderResponseDTO {

    private final Long id;
    private final LocalDateTime orderDate;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final BigDecimal totalPrice;
    private final OrderStatus status;
    private final boolean vehicleArrived;
    private final boolean accessoriesConfirmed;
    private final boolean installationCompleted;
    private final Long sellerId;
    private final String sellerName;
    private final ClientResponseDTO client;
    private final CarResponseDTO car;
    private final List<AccessoryResponseDTO> accessories;
    private final VehicleEntrySummaryDTO vehicleEntry;

    public OrderResponseDTO(Long id, LocalDateTime orderDate, LocalDateTime createdAt, LocalDateTime updatedAt,
            BigDecimal totalPrice,
            OrderStatus status, boolean vehicleArrived, boolean accessoriesConfirmed, boolean installationCompleted,
            Long sellerId, String sellerName, ClientResponseDTO client, CarResponseDTO car,
            List<AccessoryResponseDTO> accessories,
            VehicleEntrySummaryDTO vehicleEntry) {
        this.id = id;
        this.orderDate = orderDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.totalPrice = totalPrice;
        this.status = status;
        this.vehicleArrived = vehicleArrived;
        this.accessoriesConfirmed = accessoriesConfirmed;
        this.installationCompleted = installationCompleted;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.client = client;
        this.car = car;
        this.accessories = accessories;
        this.vehicleEntry = vehicleEntry;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public boolean isVehicleArrived() {
        return vehicleArrived;
    }

    public boolean isAccessoriesConfirmed() {
        return accessoriesConfirmed;
    }

    public boolean isInstallationCompleted() {
        return installationCompleted;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getSellerName() {
        return sellerName;
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

    public VehicleEntrySummaryDTO getVehicleEntry() {
        return vehicleEntry;
    }
}
