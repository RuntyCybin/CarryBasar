package com.carry.basar.model.dto.user;

public class UpdateUserResponse {
  private String username;
  private String email;
  private String role;

  public UpdateUserResponse() {
  }

  public UpdateUserResponse(String username, String email, String role) {
    this.username = username;
    this.email = email;
    this.role = role;
  }

  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  public String getRole() {
    return role;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setRole(String role) {
    this.role = role;
  }
}
