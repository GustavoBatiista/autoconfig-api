package com.gustavobatista.autoconfig.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gustavobatista.autoconfig.dto.AuthMeResponseDTO;
import com.gustavobatista.autoconfig.dto.AuthRequestDTO;
import com.gustavobatista.autoconfig.dto.AuthResponseDTO;
import com.gustavobatista.autoconfig.entity.User;
import com.gustavobatista.autoconfig.exception.ErrorCode;
import com.gustavobatista.autoconfig.exception.UnauthorizedException;
import com.gustavobatista.autoconfig.repository.UserRepository;
import com.gustavobatista.autoconfig.security.JwtService;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String BEARER = "Bearer";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(AuthRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.UNAUTHORIZED, "User not found"));

        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return new AuthResponseDTO(token, BEARER, user.getEmail(), user.getRole().name());
    }

    @Override
    @Transactional(readOnly = true)
    public AuthMeResponseDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED, "Not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.UNAUTHORIZED, "User not found"));

        return new AuthMeResponseDTO(user.getId(), user.getEmail(), user.getRole().name());
    }
}
