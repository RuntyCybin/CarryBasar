package com.carry.basar.service;

import com.carry.basar.model.Order;
import com.carry.basar.model.dto.OrderDto;

import com.carry.basar.model.dto.order.GetOrderResponse;
import com.carry.basar.model.dto.order.RemoveOrderDtoResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {

    Flux<OrderDto> getOrdersByUserId(Long userId);

    Mono<OrderDto> createOrder(OrderDto orderDto);

    Flux<OrderDto> getMyOrders();

    Flux<Order> getAllOrders();

    Mono<GetOrderResponse> getOrderById(Long orderId);

    Mono<RemoveOrderDtoResponse> removeOrderById(Long orderId);
}
