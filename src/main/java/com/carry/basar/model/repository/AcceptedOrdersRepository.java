package com.carry.basar.model.repository;

import com.carry.basar.model.AcceptedOrder;
import com.carry.basar.model.AcceptedOrderPk;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AcceptedOrdersRepository extends ReactiveCrudRepository<AcceptedOrder, AcceptedOrderPk> {
  Flux<AcceptedOrder> findByUserId(Long userId);
  Mono<AcceptedOrder> findByOrderIdAndUserId(Long orderId, Long userId);
  Mono<Void> deleteByOrderIdAndUserId(Long orderId, Long userId);
}
