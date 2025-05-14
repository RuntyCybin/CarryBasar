package com.carry.basar.model.dto.role;

public class UpdateRoleRequest {
  private String searchName;
  private String newName;

  public UpdateRoleRequest() {
  }

  public UpdateRoleRequest(String searchName, String newName) {
    this.searchName = searchName;
    this.newName = newName;
  }

  public String getNewName() {
    return newName;
  }

  public void setNewName(String newName) {
    this.newName = newName;
  }

  public String getSearchName() {
    return searchName;
  }

  public void setSearchName(String searchName) {
    this.searchName = searchName;
  }
}
