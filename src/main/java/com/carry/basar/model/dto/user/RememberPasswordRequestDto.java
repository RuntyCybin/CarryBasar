package com.carry.basar.model.dto.user;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class RememberPasswordRequestDto {
  private String newPassword;
  @NotBlank(message = "Email is mandatory")
  @Email(message = "Email should be valid")
  private String to;
  private String subject;

  public RememberPasswordRequestDto() {
  }

  public RememberPasswordRequestDto(String newPassword, String to, String subject) {
    this.newPassword = newPassword;
    this.to = to;
    this.subject = subject;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }
}
