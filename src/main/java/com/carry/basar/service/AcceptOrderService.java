package com.carry.basar.service;

import com.carry.basar.model.dto.accepted_order.AcceptOrderRequest;
import com.carry.basar.model.dto.accepted_order.AcceptedOrderResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AcceptOrderService {
  Mono<AcceptedOrderResponse> createOrder(AcceptOrderRequest request);
  Mono<AcceptedOrderResponse> getAcceptedOrderByPk(Long userId, Long orderId);
  Flux<AcceptedOrderResponse> getAcceptedOrdersByUserId(Long userId);
  Mono<String> removeAcceptedOrderByPk(Long orderId, Long userId);
}
