package com.carry.basar.model.dto.user;

import java.util.List;

public class UpdateUserRequest {
  private String username;
  private String email;
  private List<String> role;

  public UpdateUserRequest() {
  }

  public UpdateUserRequest(String username, String password, String email, List<String> role) {
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

  public List<String> getRole() {
    return role;
  }
}
