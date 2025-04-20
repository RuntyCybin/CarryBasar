package com.carry.basar.service.impl;

import com.carry.basar.model.repository.UserRolRepository;
import com.carry.basar.service.UserRoleService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
public class UserRoleServiceImpl implements UserRoleService {

  private final UserRolRepository userRolRepository;

  public UserRoleServiceImpl(UserRolRepository userRolRepository) {
    this.userRolRepository = userRolRepository;
  }

  @Override
  public Mono<Void> removeAllRolesForUser(Long userId) {
    return this.userRolRepository.findByUserId(userId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No roles found for user with ID: " + userId)))
            .doOnSubscribe(sub -> System.out.println("Subscribed to find roles for user with ID: " + userId))
            .doOnNext(userRole -> System.out.println("Suscritos userRole: " + userRole.getRoleId()))
            .doOnError(e -> System.out.println("Error al buscar roles para el usuario: " + e.getMessage()))
            .flatMap(userRol ->
                    userRolRepository.delete(userRol)
                            .doOnSuccess(aVoid -> System.out.println("Rol eliminado: " + userRol.getRoleId()))
                            .doOnError(error -> System.err.println("Error al eliminar rol: " + error.getMessage())))
            .then();
  }
}
