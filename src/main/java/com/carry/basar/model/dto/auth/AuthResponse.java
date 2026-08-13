package com.carry.basar.model.dto.auth;

import java.util.List;

public record AuthResponse(String jwt, String username, String email, List<String> roles, Long userid) {
}
