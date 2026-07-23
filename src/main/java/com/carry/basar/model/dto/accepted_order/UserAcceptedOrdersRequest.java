package com.carry.basar.model.dto.accepted_order;

public class UserAcceptedOrdersRequest {
  private Long userId;

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public UserAcceptedOrdersRequest() {
  }

  public UserAcceptedOrdersRequest(Long userId) {
    this.userId = userId;
  }

}
