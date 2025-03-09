package com.carry.basar.model.repository;

import com.carry.basar.model.UserRol;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRolRepository extends ReactiveCrudRepository<UserRol, Long> {
    Mono<UserRol> findByUserId(Long userId);
}
