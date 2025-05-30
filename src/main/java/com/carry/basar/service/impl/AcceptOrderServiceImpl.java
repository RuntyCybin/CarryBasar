package com.carry.basar.service.impl;

import com.carry.basar.model.AcceptedOrders;
import com.carry.basar.model.UserRol;
import com.carry.basar.model.dto.accepted_order.AcceptOrderRequest;
import com.carry.basar.model.dto.accepted_order.AcceptedOrderPk;
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
    // TODO: 1.encontrar al usuario con el id proporcionado
    return this.userRepository.findById(request.getUserId())
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
            .flatMap(user -> {
              // recogemos los roles del usuario
              return this.userRolRepository.findByUserId(user.getId())
                      .flatMap(userRols -> this.roleRepository.findById(userRols.getRoleId())
                              .filter(role -> role.getName().equals("TRANSPORTER"))
                              .flatMap(rolTransporter -> {
                                System.out.println("LLEGO");
                                // TODO: 2.encontrar el order con el id proporcionado (una vez completada la 1)
                                return this.orderRepository.findById(request.getOrderId())
                                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")))
                                        .flatMap(order -> {
                                          System.out.println("LLEGO2");
                                          // TODO: 3.guardar el acceptedOrder con shippedAt a null porque aun no se ha llevado el order
                                          AcceptedOrderPk acceptedOrderPk = new AcceptedOrderPk(order.getId(), user.getId());
                                          AcceptedOrders acceptedOrder = new AcceptedOrders(
                                                  acceptedOrderPk,
                                                  LocalDateTime.now(),
                                                  null);
                                          return this.acceptedOrdersRepository.save(acceptedOrder);
                                        });
                              })
                      )
                      .next()
                      .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                              "User " + user.getId() + " is not authorized as TRANSPORTER or failed to accept order " + request.getOrderId() + ".")));
                      // Este error se dispara si el usuario no tiene el rol TRANSPORTER,
                      // o si teniéndolo, la orden no se pudo encontrar o procesar.
            });
  }
}
