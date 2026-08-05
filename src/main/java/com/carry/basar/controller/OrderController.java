package com.carry.basar.controller;

import com.carry.basar.model.dto.order.GetOrderResponse;
import com.carry.basar.model.dto.order.RemoveOrderDtoResponse;
import org.springframework.web.bind.annotation.*;

import com.carry.basar.model.Order;
import com.carry.basar.model.dto.OrderDto;
import com.carry.basar.service.OrderService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Mono<OrderDto> createOrder(@RequestBody OrderDto orderDto) {
        return orderService.createOrder(orderDto);
    }

    @GetMapping("/userid/{userId}")
    public Flux<OrderDto> getUserOrders(@PathVariable Long userId) {
        return orderService.getOrdersByUserId(userId);
    }

    @GetMapping("/my-orders")
    public Flux<OrderDto> getMyOrders() {
        return orderService.getMyOrders();
    }

    @GetMapping("/all")
    public Flux<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{orderId}")
    public Mono<GetOrderResponse> getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId);
    }

    @DeleteMapping("/{orderId}")
    public Mono<RemoveOrderDtoResponse> removeOrderById(@PathVariable Long orderId) {
        return orderService.removeOrderById(orderId);
    }
}
