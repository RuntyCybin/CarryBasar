package com.carry.basar.controller;

import com.carry.basar.model.AcceptedOrders;
import com.carry.basar.model.dto.accepted_order.AcceptOrderRequest;
import com.carry.basar.model.dto.accepted_order.AcceptedOrderResponse;
import com.carry.basar.model.dto.accepted_order.UserAcceptedOrdersRequest;
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

  @PostMapping("/create")
  public Mono<AcceptedOrders> acceptOrder(@Valid @RequestBody AcceptOrderRequest acceptOrderRequest) {
    return acceptOrderService.createOrder(acceptOrderRequest);
  }

  @GetMapping("/get")
  public Mono<AcceptedOrders> getAcceptedOrderByPK(@Valid @RequestBody AcceptOrderRequest acceptOrderRequest) {
    return acceptOrderService.getAcceptedOrderByPk(acceptOrderRequest);
  }

  @GetMapping("/getAcceptedOrders/{userId}")
  public Flux<AcceptedOrderResponse> getAcceptedOrdersByUserId(@PathVariable Long userId) {
    return acceptOrderService.getAcceptedOrdersByUserId(userId);
  }

  @DeleteMapping("/delete")
  public Mono<String> deleteAcceptedOrder(@Valid @RequestBody AcceptOrderRequest acceptOrderRequest) {
    return acceptOrderService.removeAcceptedOrderByPk(acceptOrderRequest);
  }

}
