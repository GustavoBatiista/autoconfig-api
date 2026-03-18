package com.gustavobatista.autoconfig.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.gustavobatista.autoconfig.enums.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;
    @Column(name = "total_price", nullable = false)
    private Double totalPrice;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client clientId;
    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car carId;
    @ManyToMany
    @JoinTable(name = "order_accessory", joinColumns = @JoinColumn(name = "order_id"), inverseJoinColumns = @JoinColumn(name = "accessory_id"))
    private List<Accessory> accessories;

    
    public Order() {
    }

    public Order(Long id, LocalDateTime orderDate, Double totalPrice, OrderStatus status, User userId, Client clientId,
            Car carId,

            List<Accessory> accessories) {
        this.id = id;
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
        this.status = status;
        this.userId = userId;
        this.clientId = clientId;
        this.carId = carId;

        this.accessories = accessories;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Client getClientId() {
        return clientId;
    }

    public void setClientId(Client clientId) {
        this.clientId = clientId;
    }

    public Car getCarId() {
        return carId;
    }

    public void setCarId(Car carId) {
        this.carId = carId;
    }

    public List<Accessory> getAccessories() {
        return accessories;
    }

    public void setAccessories(List<Accessory> accessories) {
        this.accessories = accessories;
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
        Order other = (Order) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Order [id=" + id + ", orderDate=" + orderDate + ", totalPrice=" + totalPrice + ", status=" + status
                + ", userId=" + userId + ", clientId=" + clientId + ", carId=" + carId + ",accessories=" + accessories
                + "]";
    }

}
