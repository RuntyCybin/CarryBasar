package com.carry.basar.model.dto.user;

import java.util.Set;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Username is mandatory")
        @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters") String username,

        @NotBlank(message = "Password is mandatory")
        @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters") String password,

        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email should be valid") String email,

        @NotNull(message = "Debe asignar al menos un rol al usuario") Set<String> roles) {
}
