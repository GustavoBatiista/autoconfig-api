package com.gustavobatista.autoconfig.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.gustavobatista.autoconfig.entity.User;
import com.gustavobatista.autoconfig.enums.Role;
import com.gustavobatista.autoconfig.repository.UserRepository;

/**
 * Carrega o usuário pelo email (subject do JWT) e monta {@link UserDetails} com senha hash e
 * authorities alinhadas ao {@link com.gustavobatista.autoconfig.security.SecurityConfig} (ex.: {@code ROLE_ADMIN}).
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(toAuthority(user.getRole()))))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    private static String toAuthority(Role role) {
        if (role == null) {
            throw new IllegalStateException("User has no role assigned");
        }
        String name = role.name();
        return name.startsWith("ROLE_") ? name : "ROLE_" + name;
    }
}