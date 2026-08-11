package com.carry.basar.model.dto.user;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class ChangePasswordRequestDto {
  private String newPassword;
  @NotBlank(message = "Email is mandatory")
  @Email(message = "Email should be valid")
  private String userEmail;

  public ChangePasswordRequestDto() {
  }

  public ChangePasswordRequestDto(String newPassword, String userEmail) {
    this.newPassword = newPassword;
    this.userEmail = userEmail;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public void setUserEmail(String userEmail) {
    this.userEmail = userEmail;
  }
}
