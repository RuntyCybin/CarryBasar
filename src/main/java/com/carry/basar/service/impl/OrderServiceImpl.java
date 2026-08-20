package com.carry.basar.service.impl;

import java.time.LocalDateTime;

import com.carry.basar.model.dto.order.GetOrderResponse;
import com.carry.basar.model.dto.order.RemoveOrderResponse;
import com.carry.basar.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

  private final OrderRepository orderRepository;

  private final UserRepository userRepository;

  private final EmailService emailService;

  public OrderServiceImpl(OrderRepository orderRepository,
                          UserRepository userRepository, EmailService emailService) {
    this.userRepository = userRepository;
    this.orderRepository = orderRepository;
    this.emailService = emailService;
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
                        log.info("Create order - User: {} due date: {}", user.getEmail(), orderDto.dueDate());
                        Order order = new Order();
                        order.setDescription(orderDto.description());
                        order.setVol(orderDto.volume());
                        order.setOrderDate(LocalDateTime.now());
                        order.setDueDate(orderDto.dueDate());
                        order.setUserId(user.getId());
                        order.setFromLocation(orderDto.fromLocation());
                        order.setToLocation(orderDto.toLocation());
                        order.setPrice(orderDto.price());
                        return orderRepository.save(order)
                                .flatMap(savedOrder -> {
                                  return Mono.just(new OrderDto(
                                          savedOrder.getId(),
                                          savedOrder.getDescription(),
                                          savedOrder.getVol(),
                                          savedOrder.getOrderDate(),
                                          savedOrder.getDueDate(),
                                          savedOrder.getToLocation(),
                                          savedOrder.getFromLocation(),
                                          savedOrder.getPrice()));
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
                                      order.getDueDate(),
                                      order.getToLocation(),
                                      order.getFromLocation(),
                                      order.getPrice()));
                    }));
  }

  @Override
  public Flux<OrderDto> getAllOrders() {
    return orderRepository.findAll()
            .switchIfEmpty(Flux.error(new RuntimeException("No orders found")))
            .doOnError(throwable -> log.error("Error: {}", throwable.getMessage()))
            .doOnNext(ord -> log.info("Order: {} - {}", ord.getDescription(), ord.getOrderDate()))
            .map(order -> new OrderDto(
                    order.getId(),
                    order.getDescription(),
                    order.getVol(),
                    order.getOrderDate(),
                    order.getDueDate(),
                    order.getToLocation(),
                    order.getFromLocation(),
                    order.getPrice()));
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
  public Mono<RemoveOrderResponse> removeOrderById(Long orderId) {
    return getAuthenticatedUsername()
            .flatMap(username -> {
              return userRepository.findByName(username)
                      .switchIfEmpty(Mono.error(new RuntimeException("User not found by name")))
                      .doOnNext(userAux -> log.info("User found: {}", userAux.getEmail()))
                      .flatMap(user -> {
                        return orderRepository.findById(orderId)
                                .flatMap(orderRepository::delete)
                                .thenReturn(new RemoveOrderResponse("Order " + orderId + " was removed successfully"));
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
                log.error("User was not authenticated");
              }
              return null;
            })
            .switchIfEmpty(Mono.error(new RuntimeException("Authentication failed")));
  }

  @Override
  public Mono<String> sendSuggestedPrice(Long orderId, Double suggestedPrice) {
    return getAuthenticatedUsername().flatMap(username -> {
      return userRepository.findByName(username)
              .switchIfEmpty(Mono.error(new RuntimeException("User not found by name")))
              .doOnNext(userAux -> log.info("User found: {}", userAux.getEmail()))
              .flatMap(user -> {
                return orderRepository.findById(orderId)
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")))
                        .flatMap(order -> {
                          return userRepository.findById(order.getUserId())
                                  .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                                  .flatMap(orderOwnerUser ->
                                          emailService.sendAsync(orderOwnerUser.getEmail(),
                                                  "Suggested price",
                                                  "The transporter suggested you this price: " + suggestedPrice)
                                                  .thenReturn("Email sent to the client with the suggested price: " + suggestedPrice));
                        });

              });
    });
  }
}
