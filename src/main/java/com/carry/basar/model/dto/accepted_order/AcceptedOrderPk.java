package com.carry.basar.model.dto.accepted_order;

import org.springframework.data.relational.core.mapping.Column;

import java.util.Objects;

public class AcceptedOrderPk {

  @Column("orderid")
  private Long orderId;

  @Column("userid")
  private Long userId;

  public AcceptedOrderPk() {
  }

  public AcceptedOrderPk(Long orderId, Long userId) {
    this.orderId = orderId;
    this.userId = userId;
  }

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false; // Corregido para evitar ClassCastException
    AcceptedOrderPk that = (AcceptedOrderPk) o;
    return Objects.equals(orderId, that.orderId) &&
            Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orderId, userId);
  }
}
