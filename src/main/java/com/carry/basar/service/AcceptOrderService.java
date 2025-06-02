package com.carry.basar.service;

import com.carry.basar.model.AcceptedOrders;
import com.carry.basar.model.dto.accepted_order.AcceptOrderRequest;
import reactor.core.publisher.Mono;

public interface AcceptOrderService {
  Mono<AcceptedOrders> createOrder(AcceptOrderRequest request);
  Mono<AcceptedOrders> getAcceptedOrderByPk(AcceptOrderRequest request);
}
