package com.carry.basar.service.impl;

import java.time.LocalDateTime;

import com.carry.basar.model.dto.order.GetOrderResponse;
import com.carry.basar.model.dto.order.RemoveOrderDtoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.carry.basar.model.Order;
import com.carry.basar.model.dto.OrderDto;
import com.carry.basar.model.repository.OrderRepository;
import com.carry.basar.model.repository.UserRepository;
import com.carry.basar.service.OrderService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepository;

  private final UserRepository userRepository;

  public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository) {
    this.userRepository = userRepository;
    this.orderRepository = orderRepository;
  }

  @Override
  public Flux<OrderDto> getOrdersByUserId(Long userId) {
    return userRepository.findById(userId)
        .switchIfEmpty(Mono.error(
            new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
        .flatMapMany(user -> {
          System.out.println("User ID4: " + user.getEmail());
          return orderRepository.findByUserId(userId)
              .switchIfEmpty(Flux.error(
                  new ResponseStatusException(
                      HttpStatus.NOT_FOUND,
                      "No orders found for user")))
              .map(order -> {
                System.out.println("Order ID: " + order.getId());
                return new OrderDto(order.getDescription(),
                    order.getVol());
              });
        });
  }

  @Override
  public Mono<OrderDto> createOrder(OrderDto orderDto) {
    return getAuthenticatedUsername()
        .flatMap(username -> {
          return userRepository.findByName(username)
              .switchIfEmpty(
                  Mono.error(new ResponseStatusException(
                      HttpStatus.NOT_FOUND,
                      "User not found")))
              .flatMap(user -> {
                System.out.println("Create order - User: " + user.getEmail() + " due date: " + orderDto.dueDate());
                Order order = new Order();
                order.setDescription(orderDto.description());
                order.setVol(orderDto.volume());
                order.setOrderDate(LocalDateTime.now());
                order.setDueDate(orderDto.dueDate());
                order.setUserId(user.getId());
                return orderRepository.save(order)
                    .flatMap(savedOrder -> {
                      return Mono.just(new OrderDto(
                          savedOrder.getId(),
                          savedOrder.getDescription(),
                          savedOrder.getVol(),
                          savedOrder.getOrderDate(),
                          savedOrder.getDueDate()));
                    });
              });
        });
  }

  @Override
  public Flux<OrderDto> getMyOrders() {
    return getAuthenticatedUsername()
        .flatMapMany(username -> userRepository.findByName(username)
            .switchIfEmpty(Mono.error(new RuntimeException("User not found by name")))
            .doOnNext(userAux -> System.out.println("User: " + userAux.getEmail()))
            .flatMapMany(user -> {
              return orderRepository.findByUserId(user.getId())
                  .doOnNext(ord -> System.out.println("Order: " + ord.getDescription() + " - " + ord.getOrderDate()))
                  .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No orders found for you")))
                  .map(order -> new OrderDto(
                          order.getId(),
                      order.getDescription(),
                      order.getVol(),
                      order.getOrderDate(),
                      order.getDueDate()));
            }));
  }

  @Override
  public Flux<Order> getAllOrders() {
    return orderRepository.findAll()
        .doOnNext(ord -> System.out.println("Order: " + ord.getDescription() + " - " + ord.getOrderDate()))
        .switchIfEmpty(Flux.error(new RuntimeException("No orders found")))
        .doOnError(throwable -> System.out.println("Error: " + throwable.getMessage()))
        .doOnNext(ord -> System.out.println("Order: " + ord.getDescription() + " - " + ord.getOrderDate()));
  }

  @Override
  public Mono<GetOrderResponse> getOrderById(Long orderId) {
    return orderRepository.findById(orderId)
            .switchIfEmpty(Mono.error(new RuntimeException("Order not found")))
            .flatMap(order -> {
              return userRepository.findById(order.getUserId())
                      .flatMap(user -> {
                         return Mono.just(new GetOrderResponse(
                                 order.getDescription(),
                                 order.getVol(),
                                 order.getOrderDate().toString(),
                                 user.getName()
                         ));
                      });

            });
  }

  @Override
  public Mono<RemoveOrderDtoResponse> removeOrderById(Long orderId) {
    return getAuthenticatedUsername()
            .flatMap(username -> {
              return userRepository.findByName(username)
                      .switchIfEmpty(Mono.error(new RuntimeException("User not found by name")))
                      .doOnNext(userAux -> System.out.println("User: " + userAux.getEmail()))
                      .flatMap(user -> {
                        return orderRepository.findById(orderId)
                                .flatMap(orderRepository::delete)
                                .thenReturn(new RemoveOrderDtoResponse("Order " + orderId + " was removed successfully"));
                      });
            });
  }

  private Mono<String> getAuthenticatedUsername() {
    return ReactiveSecurityContextHolder.getContext()
        .map(ctx -> {
          Authentication authentication = ctx.getAuthentication();
          if (authentication != null && authentication.isAuthenticated()) {
            // Devuelve el username extraído del token
            return authentication.getName();
          } else {
            System.out.println("User was not authenticated");
          }
          return null;
        })
        .switchIfEmpty(Mono.error(new RuntimeException("Authentication failed")));
  }
}
