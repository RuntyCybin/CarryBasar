package com.carry.basar.model.dto.accepted_order;

import java.time.LocalDateTime;

public class AcceptedOrderResponse {
  private Long orderId;
  private String orderDesc;
  private Long userId;
  private Integer vol;
  private LocalDateTime createdAt;
  private LocalDateTime shippedAt;

  public AcceptedOrderResponse (Long orderId, String orderDesc, Long userId,
                                LocalDateTime createdAt, LocalDateTime shippedAt, Integer vol) {
    this.orderId = orderId;
    this.orderDesc = orderDesc;
    this.userId = userId;
    this.createdAt = createdAt;
    this.shippedAt = shippedAt;
    this.vol = vol;
  }

  public AcceptedOrderResponse() {
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public String getOrderDesc() {
    return orderDesc;
  }

  public void setOrderDesc(String orderDesc) {
    this.orderDesc = orderDesc;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getShippedAt() {
    return shippedAt;
  }

  public void setShippedAt(LocalDateTime shippedAt) {
    this.shippedAt = shippedAt;
  }

  public Integer getVol() {
    return vol;
  }

  public void setVol(Integer vol) {
    this.vol = vol;
  }
}
