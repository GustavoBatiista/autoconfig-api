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

/**
 * Cria um utilizador {@link Role#ROLE_ADMIN} local quando o perfil {@code dev} está ativo e o email
 * ainda não existe. Apenas para desenvolvimento (ex.: H2).
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevAdminInitializer implements ApplicationRunner {

    private static final String DEV_ADMIN_EMAIL = "admin@dev.com";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(DEV_ADMIN_EMAIL)) {
            return;
        }
        User admin = new User();
        admin.setName("Admin");
        admin.setLastName("Dev");
        admin.setNickName("admin");
        admin.setEmail(DEV_ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ROLE_ADMIN);
        userRepository.save(admin);
    }
}
