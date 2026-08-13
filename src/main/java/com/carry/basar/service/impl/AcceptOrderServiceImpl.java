package com.carry.basar.service.impl;

import com.carry.basar.model.AcceptedOrders;
import com.carry.basar.model.dto.accepted_order.AcceptOrderRequest;
import com.carry.basar.model.dto.accepted_order.AcceptedOrderResponse;
import com.carry.basar.model.repository.*;
import com.carry.basar.service.AcceptOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AcceptOrderServiceImpl implements AcceptOrderService {

  private final AcceptedOrdersRepository acceptedOrdersRepository;
  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final UserRolRepository userRolRepository;

  public AcceptOrderServiceImpl(AcceptedOrdersRepository acceptedOrdersRepository, RoleRepository roleRepository,
                                UserRepository userRepository, OrderRepository orderRepository, UserRolRepository userRolRepository) {
    this.acceptedOrdersRepository = acceptedOrdersRepository;
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
    this.orderRepository = orderRepository;
    this.userRolRepository = userRolRepository;
  }

  @Override
  public Mono<AcceptedOrderResponse> createOrder(AcceptOrderRequest request) {
    // 1.encontrar al usuario con el id proporcionado
    return this.userRepository.findById(request.userId())
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
            .flatMap(user -> {
              // recogemos los roles del usuario
              return this.userRolRepository.findByUserId(user.getId())
                      .flatMap(userRols -> this.roleRepository.findById(userRols.getRoleId())
                              .filter(role -> role.getName().equals("TRANSPORTER"))
                              .flatMap(rolTransporter -> {
                                // 2.encontrar el order con el id proporcionado (una vez completada la 1)
                                return this.orderRepository.findById(request.orderId())
                                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")))
                                        .flatMap(order -> {
                                          // 3.null porque aun no se ha llevado el order
                                          AcceptedOrders acceptedOrder = new AcceptedOrders(
                                                  request.userId(),
                                                  request.orderId(),
                                                  request.shipAt(),
                                                  request.shipTo(),
                                                  order.getDescription(),
                                                  order.getVol());
                                          return this.acceptedOrdersRepository.save(acceptedOrder)
                                                  .flatMap(acceptedOrders -> {
                                                    AcceptedOrderResponse acceptedOrderResponse = new AcceptedOrderResponse(
                                                            acceptedOrders.getOrderId(),
                                                            acceptedOrders.getDescription(),
                                                            acceptedOrders.getUserId(),
                                                            acceptedOrders.getCreatedAt(),
                                                            acceptedOrders.getShippedAt(),
                                                            acceptedOrders.getVol()
                                                    );
                                                    return orderRepository.delete(order)
                                                            .thenReturn(acceptedOrderResponse);
                                                  });
                                        });
                              })
                      )
                      .next()
                      // Este error se dispara si el usuario no tiene el rol TRANSPORTER,
                      // o si teniéndolo, la orden no se pudo encontrar o procesar.
                      .switchIfEmpty(Mono.error(new ResponseStatusException(
                              HttpStatus.FORBIDDEN,
                              "User " + user.getId() + " is not authorized as TRANSPORTER or failed to accept order "
                                      + request.orderId() + ".")));
            });
  }

  @Override
  public Mono<AcceptedOrderResponse> getAcceptedOrderByPk(Long userId, Long orderId) {
    // 1.encontrar al usuario con el id proporcionado
    return this.userRepository.findById(userId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
            .flatMap(user -> {
              // 2.encontrar el order con el id proporcionado
              return acceptedOrdersRepository.findByOrderIdAndUserId(orderId, userId)
                      .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Accepted order not found")))
                      .map(acceptedOrders -> {
                        return new AcceptedOrderResponse(
                                acceptedOrders.getOrderId(),
                                acceptedOrders.getDescription(),
                                acceptedOrders.getUserId(),
                                acceptedOrders.getCreatedAt(),
                                acceptedOrders.getShippedAt(),
                                acceptedOrders.getVol()
                        );
                      });
            });
  }

  @Override
  public Flux<AcceptedOrderResponse> getAcceptedOrdersByUserId(Long userId) {
    // 1. Encontrar al usuario con el id proporcionado
    return this.userRepository.findById(userId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
            .flatMapMany(user ->
                    // 2. Verificar que el usuario tiene rol TRANSPORTER
                    this.userRolRepository.findByUserId(user.getId())
                            .flatMap(userRols -> this.roleRepository.findById(userRols.getRoleId()))
                            .filter(role -> "TRANSPORTER".equals(role.getName()))
                            .hasElements()
                            .flatMapMany(rolTransporter -> {
                              if (!rolTransporter) {
                                return Flux.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not TRANSPORTER"));
                              }

                              // 3. Cargar sus accepted orders y mapear la respuesta
                              return acceptedOrdersRepository.findByUserId(user.getId())
                                      .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No accepted orders found")))
                                      .map(acceptedOrder -> new AcceptedOrderResponse(
                                              acceptedOrder.getOrderId(),
                                              acceptedOrder.getDescription(),
                                              acceptedOrder.getUserId(),
                                              acceptedOrder.getCreatedAt(),
                                              acceptedOrder.getShippedAt(),
                                              acceptedOrder.getVol()
                                      ));
                            })
            );
  }

  @Override
  public Mono<String> removeAcceptedOrderByPk(Long orderId, Long userId) {
    // 1. encontrar al usuario con la id de la request
    return this.userRepository.findById(userId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
            .flatMap(user -> {
              return acceptedOrdersRepository.findByOrderIdAndUserId(orderId, userId)
                      .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Accepted Order not found")))
                      .flatMap(acceptedOrder -> acceptedOrdersRepository.deleteByOrderIdAndUserId(orderId, userId)
                              .thenReturn("SUCCESS"));
            });
  }
}
