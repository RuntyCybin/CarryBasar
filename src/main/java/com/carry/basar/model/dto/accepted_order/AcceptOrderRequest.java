package com.carry.basar.model.dto.accepted_order;

public class AcceptOrderRequest {
  private Long orderId;
  private Long userId;

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
}
