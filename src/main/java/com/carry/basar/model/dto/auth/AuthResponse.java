package com.carry.basar.model.dto.auth;

public class AuthResponse {
  private String jwt;
  private String username;
  private String email;

  public AuthResponse(String jwt, String username, String email) {
    this.jwt = jwt;
    this.username = username;
    this.email = email;
  }

  public String getJwt() {
    return jwt;
  }

  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  public void setJwt(String jwt) {
    this.jwt = jwt;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
