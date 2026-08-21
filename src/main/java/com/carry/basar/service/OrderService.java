package com.carry.basar.service;

import com.carry.basar.model.Order;
import com.carry.basar.model.dto.OrderDto;

import com.carry.basar.model.dto.order.GetOrderResponse;
import com.carry.basar.model.dto.order.RemoveOrderResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {

    Mono<OrderDto> createOrder(OrderDto orderDto);

    Flux<OrderDto> getMyOrders();

    Flux<OrderDto> getAllOrders();

    Mono<GetOrderResponse> getOrderById(Long orderId);

    Mono<RemoveOrderResponse> removeOrderById(Long orderId);

    Mono<String> sendSuggestedPrice(Long orderId, Double suggestedPrice);
}
