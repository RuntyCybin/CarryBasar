package com.carry.basar.controller;

import com.carry.basar.model.AcceptedOrders;
import com.carry.basar.model.dto.accepted_order.AcceptOrderRequest;
import com.carry.basar.service.AcceptOrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
