package com.carry.basar.service;

import com.carry.basar.model.Order;
import com.carry.basar.model.dto.OrderDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {

    Flux<OrderDto> getOrdersByUserId(Long userId);

    Mono<OrderDto> createOrder(OrderDto orderDto);

    Flux<OrderDto> getMyOrders();

    Flux<Order> getAllOrders();
}
