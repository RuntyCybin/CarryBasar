package com.carry.basar.model.dto.accepted_order;

import java.time.LocalDateTime;

public class AcceptOrderRequest {
  private Long orderId;
  private Long userId;
  private LocalDateTime shipAt;
  private LocalDateTime shipTo;
  private String description;
  private Integer volumen;

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

  public LocalDateTime getShipAt() {
    return shipAt;
  }

  public void setShipAt(LocalDateTime shipAt) {
    this.shipAt = shipAt;
  }

  public LocalDateTime getShipTo() {
    return shipTo;
  }

  public void setShipTo(LocalDateTime shipTo) {
    this.shipTo = shipTo;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getVolumen() {
    return volumen;
  }

  public void setVolumen(Integer volumen) {
    this.volumen = volumen;
  }
}
