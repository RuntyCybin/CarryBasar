package com.carry.basar.model.dto.auth;

import java.util.List;

public class AuthResponse {
  private String jwt;
  private String username;
  private String email;
  private List<String> roles;

  public AuthResponse(String jwt, String username, String email, List<String> roles) {
    this.jwt = jwt;
    this.username = username;
    this.email = email;
    this.roles = roles;
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

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }
}
