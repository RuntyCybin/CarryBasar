package com.carry.basar.service.impl;

import com.carry.basar.model.AcceptedOrders;
import com.carry.basar.model.UserRol;
import com.carry.basar.model.dto.accepted_order.AcceptOrderRequest;
import com.carry.basar.model.dto.accepted_order.AcceptedOrderPk;
import com.carry.basar.model.dto.accepted_order.AcceptedOrderResponse;
import com.carry.basar.model.dto.accepted_order.UserAcceptedOrdersRequest;
import com.carry.basar.model.repository.*;
import com.carry.basar.service.AcceptOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

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
  public Mono<AcceptedOrders> createOrder(AcceptOrderRequest request) {
    // 1.encontrar al usuario con el id proporcionado
    return this.userRepository.findById(request.getUserId())
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
            .flatMap(user -> {
              // recogemos los roles del usuario
              return this.userRolRepository.findByUserId(user.getId())
                      .flatMap(userRols -> this.roleRepository.findById(userRols.getRoleId())
                              .filter(role -> role.getName().equals("TRANSPORTER"))
                              .flatMap(rolTransporter -> {
                                // 2.encontrar el order con el id proporcionado (una vez completada la 1)
                                return this.orderRepository.findById(request.getOrderId())
                                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")))
                                        .flatMap(order -> {
                                          // 3.null porque aun no se ha llevado el order
                                          AcceptedOrderPk acceptedOrderPk = new AcceptedOrderPk(request.getOrderId(), request.getUserId());
                                          AcceptedOrders acceptedOrder = new AcceptedOrders(
                                                  acceptedOrderPk.getUserId(),
                                                  acceptedOrderPk.getOrderId(),
                                                  request.getShipAt(),
                                                  request.getShipTo(),
                                                  order.getDescription(),
                                                  order.getVol());
                                          return this.acceptedOrdersRepository.save(acceptedOrder)
                                                  .flatMap(acceptedOrders -> {
                                                    return orderRepository.delete(order)
                                                            .thenReturn(acceptedOrder);
                                                  });
                                        });
                              })
                      )
                      .next()
                      // Este error se dispara si el usuario no tiene el rol TRANSPORTER,
                      // o si teniéndolo, la orden no se pudo encontrar o procesar.
                      .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                              "User " + user.getId() + " is not authorized as TRANSPORTER or failed to accept order " + request.getOrderId() + ".")));
            });
  }

  @Override
  public Mono<AcceptedOrders> getAcceptedOrderByPk(AcceptOrderRequest request) {
    // TODO: 1.encontrar al usuario con el id proporcionado
    return this.userRepository.findById(request.getUserId())
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
            .flatMap(user -> {
              // TODO: 2.encontrar el order con el id proporcionado
              return orderRepository.findById(request.getOrderId())
                      .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")))
                      .flatMap(order -> {
                        // TODO: 3.encontrar el acceptedOrder con los ids de user y order proporcionados
                        return acceptedOrdersRepository.findByOrderIdAndUserId(order.getId(), user.getId())
                                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Accepted order not found")));
                      });
            });
  }

  @Override
  public Flux<AcceptedOrderResponse> getAcceptedOrdersByUserId(Long userId) {
    // TODO: 1.encontrar al usuario con el id proporcionado
    return this.userRepository.findById(userId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
            .flatMapMany(user ->
                    // 1) Verificar que el usuario tiene rol TRANSPORTER
                    this.userRolRepository.findByUserId(user.getId())
                            .flatMap(userRols -> this.roleRepository.findById(userRols.getRoleId()))
                            .filter(role -> "TRANSPORTER".equals(role.getName()))
                            .hasElements()
                            .flatMapMany(rolTransporter -> {
                              if (!rolTransporter) {
                                return Flux.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not TRANSPORTER"));
                              }

                              // 2) Cargar sus accepted orders y mapear la respuesta
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
  public Mono<String> removeAcceptedOrderByPk(AcceptOrderRequest request) {
    // TODO: 1. encontrar al usuario con la id de la request
    return this.userRepository.findById(request.getUserId())
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
            .flatMap(user -> {
              // TODO: 2.encontrar el order por id proporcionado
              return orderRepository.findById(request.getOrderId())
                      .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")))
                      .flatMap(order -> {
                        // TODO: 3.generar el entity para la eliminacion
                        AcceptedOrderPk acceptedOrderPk = new AcceptedOrderPk(request.getOrderId(), request.getUserId());
                        AcceptedOrders acceptedOrders = new AcceptedOrders(
                                acceptedOrderPk.getUserId(),
                                acceptedOrderPk.getOrderId(),
                                request.getShipAt(),
                                request.getShipTo(),
                                order.getDescription(),
                                order.getVol()
                        );
                        // 4.eliminar el order y devolver "SUCCESS" si se ha eliminado correctamente
                        return acceptedOrdersRepository.delete(acceptedOrders)
                                .thenReturn("SUCCESS");
                      });
            });
  }


}
