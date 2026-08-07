package com.carry.basar.model.dto.user;

import java.util.List;

public class UpdateUserRequest {
  private String username;
  private String email;
  private List<String> role;
  private String newPassword;

  public UpdateUserRequest() {
  }

  public UpdateUserRequest(String username, String newPassword, String email, List<String> role) {
    this.username = username;
    this.email = email;
    this.role = role;
    this.newPassword = newPassword;
  }

  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  public List<String> getRole() {
    return role;
  }

  public String getNewPassword() {
    return newPassword;
  }
}
