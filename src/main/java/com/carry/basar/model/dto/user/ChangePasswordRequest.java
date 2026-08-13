package com.carry.basar.model.dto.user;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        String newPassword,
        @NotBlank(message = "Email is mandatory") @Email(message = "Email should be valid") String userEmail) {
}
