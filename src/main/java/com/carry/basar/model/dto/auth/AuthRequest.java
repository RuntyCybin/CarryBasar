package com.carry.basar.model.dto.auth;

import javax.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "Username is mandatory for the authentication") String username,
        @NotBlank(message = "Password is mandatory for the authentication") String password) {
}