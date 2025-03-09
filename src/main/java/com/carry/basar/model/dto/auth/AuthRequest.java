package com.carry.basar.model.dto.auth;

import javax.validation.constraints.NotBlank;

public class AuthRequest {

    @NotBlank(message = "Username is mandatory for the authentication")
    private String username;

    @NotBlank(message = "Password is mandatory for the authentication")
    private String password;

    public AuthRequest() {
    }

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
