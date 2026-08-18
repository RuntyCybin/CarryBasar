package com.carry.basar.model.repository;

import com.carry.basar.model.Role;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RoleRepository extends ReactiveCrudRepository<Role, Long> {
    Mono<Role> findByName(String name);

    @Query("SELECT * FROM roles WHERE descripcion IN ('TRANSPORTER', 'CARRY')")
    Flux<Role> findTransporterAndCarryRoles();

}
