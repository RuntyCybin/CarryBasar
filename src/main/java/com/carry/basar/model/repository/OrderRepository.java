package com.carry.basar.model.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import com.carry.basar.model.Order;
import reactor.core.publisher.Flux;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
    Flux<Order> findByUserId(Long userId);
}
