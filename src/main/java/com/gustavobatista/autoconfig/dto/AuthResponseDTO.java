package com.gustavobatista.autoconfig.dto;

public class AuthResponseDTO {

    private final String token;
    private final String type;
    private final String email;
    private final String role;

    public AuthResponseDTO(String token, String type, String email, String role) {
        this.token = token;
        this.type = type;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
