package com.carry.basar.service;

import com.carry.basar.model.dto.accepted_order.AcceptedOrderRequest;
import com.carry.basar.model.dto.accepted_order.AcceptedOrderResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AcceptOrderService {
  Mono<AcceptedOrderResponse> createOrder(AcceptedOrderRequest request);
  Mono<AcceptedOrderResponse> getAcceptedOrderByPk(Long userId, Long orderId);
  Flux<AcceptedOrderResponse> getAcceptedOrdersByUserId(Long userId);
  Mono<String> removeAcceptedOrderByPk(Long orderId, Long userId);
}
