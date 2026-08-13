package com.carry.basar.model.dto.user;

import java.util.List;

public record UpdateUserRequest(String username, String email, List<String> role, String newPassword) {
}
