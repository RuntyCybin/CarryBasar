package com.carry.basar.controller;

import com.carry.basar.model.dto.accepted_order.AcceptOrderRequest;
import com.carry.basar.model.dto.accepted_order.AcceptedOrderResponse;
import com.carry.basar.service.AcceptOrderService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/api/acceptOrder")
public class AcceptOrderController {

  private final AcceptOrderService acceptOrderService;

  public AcceptOrderController(AcceptOrderService acceptOrderService) {
    this.acceptOrderService = acceptOrderService;
  }

  /**
   * Endpoint to create an accepted order removing the normal order
   * @param acceptOrderRequest
   * @return a DTO for an accepted order
   */
  @PostMapping("/create")
  public Mono<AcceptedOrderResponse> acceptOrder(@Valid @RequestBody AcceptOrderRequest acceptOrderRequest) {
    return acceptOrderService.createOrder(acceptOrderRequest);
  }

  /**
   * Endpoint to retrieve a specific accepted order by its id and the user who accepted this order
   * @param userId
   * @param orderId
   * @return a DTO for an accepted order
   */
  @GetMapping("/get")
  public Mono<AcceptedOrderResponse> getAcceptedOrderByPk(@Valid @RequestParam Long userId, @Valid @RequestParam Long orderId) {
    return acceptOrderService.getAcceptedOrderByPk(userId, orderId);
  }

  /**
   * Endpoint to retrieve all the accepted orders for a TRANSPORTER user
   * @param userId
   * @return a list of DTO's of accepted orders of a TRANSPORTER user
   */
  @GetMapping("/getAcceptedOrders/{userId}")
  public Flux<AcceptedOrderResponse> getAcceptedOrdersByUserId(@PathVariable Long userId) {
    return acceptOrderService.getAcceptedOrdersByUserId(userId);
  }

  /**
   * Endpoint that deletes an accepted order
   * @param orderId
   * @param userId
   * @return a String success message
   */
  @DeleteMapping("/delete")
  public Mono<String> deleteAcceptedOrder(@Valid @RequestParam Long orderId, @Valid @RequestParam Long userId) {
    return acceptOrderService.removeAcceptedOrderByPk(orderId, userId);
  }

}
