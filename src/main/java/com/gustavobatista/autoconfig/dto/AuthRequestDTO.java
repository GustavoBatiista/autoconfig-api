package com.gustavobatista.autoconfig.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthRequestDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private final String email;

    @NotBlank(message = "Password is required")
    private final String password;

    public AuthRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
