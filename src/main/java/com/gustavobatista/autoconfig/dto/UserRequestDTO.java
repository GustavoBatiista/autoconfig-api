package com.gustavobatista.autoconfig.dto;

import com.gustavobatista.autoconfig.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50)
    private final String name;

    @NotBlank(message = "Last name is required")
    @Size(min = 3, max = 50)
    private final String lastName;

    @NotBlank(message = "Nick name is required")
    @Size(min = 3, max = 50)
    private final String nickName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private final String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
    private final String password;

    @NotNull(message = "Role is required")
    private final Role role;

    public UserRequestDTO(String name,
            String lastName,
            String nickName,
            String email,
            String password,
            Role role) {
        this.name = name;
        this.lastName = lastName;
        this.nickName = nickName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getNickName() {
        return nickName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

}
