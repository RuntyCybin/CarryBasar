package com.carry.basar.model.dto.user;

public class ListUsersResponse {
  private String email;
  private String username;

  public ListUsersResponse(String email, String username) {
    this.email = email;
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public String getUsername() {
    return username;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
