package com.carry.basar.model.repository;

import com.carry.basar.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    Mono<User> findByName(String name);
    Mono<User> findByEmail(String email);
}
