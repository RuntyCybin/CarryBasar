package com.carry.basar.model.dto.accepted_order;

public class UserAcceptedOrdersRequest {
  private Long userId;
  private Long orderId;

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public UserAcceptedOrdersRequest() {
  }

  public UserAcceptedOrdersRequest(Long userId, Long orderId) {
    this.userId = userId;
    this.orderId = orderId;
  }

}
