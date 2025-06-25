package com.carry.basar.model.dto.role;

public class RolesListResponse {
  private String name;

  public RolesListResponse() {
  }

  public RolesListResponse(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
