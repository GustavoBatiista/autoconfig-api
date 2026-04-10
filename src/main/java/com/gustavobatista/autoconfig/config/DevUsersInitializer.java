package com.gustavobatista.autoconfig.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.gustavobatista.autoconfig.entity.User;
import com.gustavobatista.autoconfig.enums.Role;
import com.gustavobatista.autoconfig.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevUsersInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureUser("admin@dev.com", "admin123", "Admin", "Dev", "admin", Role.ROLE_ADMIN);
        ensureUser("manager@dev.com", "manager123", "Manager", "Dev", "manager", Role.ROLE_MANAGER);
        ensureUser("seller@dev.com", "seller123", "Seller", "Dev", "seller", Role.ROLE_SELLER);
        ensureUser("vehiclestock@dev.com", "vehicle123", "Vehicle", "Stock", "vehiclestock", Role.ROLE_VEHICLE_STOCK);
        ensureUser("accessorystock@dev.com", "accessory123", "Accessory", "Stock", "accessorystock",
                Role.ROLE_ACCESSORY_STOCK);
    }

    private void ensureUser(String email, String rawPassword, String name, String lastName, String nickName, Role role) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User user = new User();
        user.setName(name);
        user.setLastName(lastName);
        user.setNickName(nickName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        userRepository.save(user);
    }
}
