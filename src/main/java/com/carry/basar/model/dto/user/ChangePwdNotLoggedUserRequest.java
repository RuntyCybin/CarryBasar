package com.carry.basar.model.dto.user;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class ChangePwdNotLoggedUserRequest {

  @NotBlank(message = "Email is mandatory")
  @Email(message = "Email should be valid")
  private String email;
  private String newPassword;

  public ChangePwdNotLoggedUserRequest() {
  }

  public ChangePwdNotLoggedUserRequest(String email, String newPassword) {
    this.email = email;
    this.newPassword = newPassword;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

}
