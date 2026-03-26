package com.gustavobatista.autoconfig.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gustavobatista.autoconfig.dto.AuthMeResponseDTO;
import com.gustavobatista.autoconfig.dto.AuthRequestDTO;
import com.gustavobatista.autoconfig.dto.AuthResponseDTO;
import com.gustavobatista.autoconfig.service.AuthService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "1 - Auth", description = "Endpoints for authentication")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthMeResponseDTO> me() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }
}
