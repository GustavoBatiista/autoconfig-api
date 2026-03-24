package com.gustavobatista.autoconfig.dto;

public class AuthMeResponseDTO {

    private final Long id;
    private final String email;
    private final String role;

    public AuthMeResponseDTO(Long id, String email, String role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
