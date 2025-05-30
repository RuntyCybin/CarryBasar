package com.carry.basar.model.repository;

import com.carry.basar.model.AcceptedOrders;
import com.carry.basar.model.dto.accepted_order.AcceptedOrderPk;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AcceptedOrdersRepository extends ReactiveCrudRepository<AcceptedOrders, AcceptedOrderPk> {
  //Mono<AcceptedOrders> createAcceptedOrder(AcceptedOrderPk acceptedOrderPk);

  //Flux<AcceptedOrders> findByUserId(Long userId);

  Mono<AcceptedOrders> save(AcceptedOrders acceptedOrders);

  //Mono<AcceptedOrders> deleteById(AcceptedOrderPk acceptedOrderPk);
}
