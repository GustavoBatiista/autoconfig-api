package com.gustavobatista.autoconfig.dto;

public class ClientResponseDTO {

    private final Long id;
    private final String name;
    private final String lastName;
    private final String phoneNumber;

    public ClientResponseDTO(Long id, String name, String lastName, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
