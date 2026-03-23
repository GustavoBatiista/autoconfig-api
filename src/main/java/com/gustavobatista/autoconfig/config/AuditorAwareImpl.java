package com.gustavobatista.autoconfig.config;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return Optional.of(userDetails.getUsername());
        }
        if (principal instanceof String name && !"anonymousUser".equals(name)) {
            return Optional.of(name);
        }
        String name = authentication.getName();
        if (name == null || "anonymousUser".equals(name)) {
            return Optional.empty();
        }
        return Optional.of(name);
    }
}
