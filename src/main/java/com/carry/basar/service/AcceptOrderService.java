package com.carry.basar.service;

import com.carry.basar.model.AcceptedOrders;
import com.carry.basar.model.dto.accepted_order.AcceptOrderRequest;
import com.carry.basar.model.dto.accepted_order.AcceptedOrderResponse;
import com.carry.basar.model.dto.accepted_order.UserAcceptedOrdersRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AcceptOrderService {
  Mono<AcceptedOrders> createOrder(AcceptOrderRequest request);
  Mono<AcceptedOrders> getAcceptedOrderByPk(AcceptOrderRequest request);
  Flux<AcceptedOrderResponse> getAcceptedOrdersByUserId(Long userId);
  Mono<String> removeAcceptedOrderByPk(AcceptOrderRequest request);
}
