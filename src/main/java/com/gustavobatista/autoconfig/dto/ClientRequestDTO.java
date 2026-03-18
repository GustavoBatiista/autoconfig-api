package com.gustavobatista.autoconfig.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ClientRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50)
    private final String name;

    @NotBlank(message = "Last name is required")
    @Size(min = 3, max = 50)
    private final String lastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\d{11}", message = "Phone must have at least 11 digits")
    private final String phoneNumber;

    public ClientRequestDTO(String name, String lastName, String phoneNumber) {
        this.name = name;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
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
