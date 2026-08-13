package com.carry.basar.model.repository;

import com.carry.basar.model.AcceptedOrders;
import com.carry.basar.model.AcceptedOrderPk;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AcceptedOrdersRepository extends ReactiveCrudRepository<AcceptedOrders, AcceptedOrderPk> {
  Flux<AcceptedOrders> findByUserId(Long userId);
  Mono<AcceptedOrders> findByOrderIdAndUserId(Long orderId, Long userId);
  Mono<Void> deleteByOrderIdAndUserId(Long orderId, Long userId);
}
