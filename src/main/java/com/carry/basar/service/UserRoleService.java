package com.carry.basar.service;

import reactor.core.publisher.Mono;

public interface UserRoleService {
  Mono<Void> removeAllRolesForUser(Long userId);
}
