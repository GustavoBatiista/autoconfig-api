package com.gustavobatista.autoconfig.support;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.gustavobatista.autoconfig.entity.Car;
import com.gustavobatista.autoconfig.entity.Client;
import com.gustavobatista.autoconfig.entity.Order;
import com.gustavobatista.autoconfig.entity.User;
import com.gustavobatista.autoconfig.enums.OrderStatus;
import com.gustavobatista.autoconfig.enums.Role;

public final class TestFixtures {

    public static final String ADMIN_EMAIL = "admin@test.com";
    public static final String MANAGER_EMAIL = "manager@test.com";
    public static final String SELLER_EMAIL = "seller@test.com";

    private TestFixtures() {
    }

    public static User userAdmin() {
        return new User(1L, "Admin", "User", "admin", ADMIN_EMAIL, "hash", Role.ROLE_ADMIN);
    }

    public static User userManager() {
        return new User(2L, "Man", "Ager", "mgr", MANAGER_EMAIL, "hash", Role.ROLE_MANAGER);
    }

    public static User userSeller() {
        return new User(3L, "Sel", "Ler", "sel", SELLER_EMAIL, "hash", Role.ROLE_SELLER);
    }

    public static Client client(Long id) {
        return new Client(id, "John", "Doe", "11999999999");
    }

    public static Car car(Long id) {
        return new Car(id, "Ford", "Focus", "SEL");
    }

    public static Order order(Long id, User seller, Client client, Car car) {
        return new Order(
                id,
                LocalDateTime.of(2025, 1, 1, 12, 0),
                new BigDecimal("100.00"),
                OrderStatus.WAITING_VEHICLE,
                seller,
                client,
                car,
                new ArrayList<>(),
                false,
                false,
                false,
                false);
    }
}
