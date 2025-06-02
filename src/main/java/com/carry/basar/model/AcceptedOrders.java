package com.carry.basar.model;

import com.carry.basar.model.dto.accepted_order.AcceptedOrderPk;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("accepted_orders")
public class AcceptedOrders {

  @Column("userid")
  private Long userId;

  @Column("orderid")
  private Long orderId;

  @Column("createdAt")
  private LocalDateTime createdAt;

  @Column("shippedAt")
  private LocalDateTime shippedAt;

  public AcceptedOrders(AcceptedOrderPk pk, LocalDateTime acceptedAt, LocalDateTime shippedAt) {
    if (null == pk) {
      throw new IllegalArgumentException("AcceptedOrderPk cannot be null");
    }
    this.userId = pk.getUserId();
    this.orderId = pk.getOrderId();
    this.createdAt = acceptedAt;
    this.shippedAt = shippedAt;
  }

  public AcceptedOrders(Long userId, Long orderId, LocalDateTime createdAt, LocalDateTime shippedAt) {
    this.userId = userId;
    this.orderId = orderId;
    this.createdAt = createdAt;
    this.shippedAt = shippedAt;
  }

  public AcceptedOrders() {
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
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
}
