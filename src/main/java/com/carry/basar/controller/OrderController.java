package com.carry.basar.controller;

import com.carry.basar.model.dto.order.GetOrderResponse;
import com.carry.basar.model.dto.order.RemoveOrderResponse;
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

    /**
     * Endpoint to create an order if you are a CLIENT user
     * @param orderDto a body with a name, description, etc
     * @return a OrderDto object
     */
    @PostMapping
    public Mono<OrderDto> createOrder(@RequestBody OrderDto orderDto) {
        return orderService.createOrder(orderDto);
    }

    @GetMapping("/my-orders")
    public Flux<OrderDto> getMyOrders() {
        return orderService.getMyOrders();
    }

    @GetMapping("/all")
    public Flux<OrderDto> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{orderId}")
    public Mono<GetOrderResponse> getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId);
    }

    @DeleteMapping("/{orderId}")
    public Mono<RemoveOrderResponse> removeOrderById(@PathVariable Long orderId) {
        return orderService.removeOrderById(orderId);
    }

    /**
     * Endpoint to email a CLIENT user with a suggested price
     * instead of the order price
     * @param orderId : ID of the order to be rejected
     * @return a message with an order ID and with a suggested price
     */
    @PostMapping("/suggestedPrice")
    public Mono<String> suggestedPrice(@RequestParam Long orderId, @RequestParam Double price) {
        return Mono.just("Order ID: " + orderId + " Price: " + price);
    }
}
