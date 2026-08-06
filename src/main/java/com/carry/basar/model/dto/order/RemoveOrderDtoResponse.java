package com.carry.basar.model.dto.order;

public class RemoveOrderDtoResponse {
  private String message;

  public RemoveOrderDtoResponse(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
