package com.carry.basar.service.impl;

import com.carry.basar.model.repository.UserRolRepository;
import com.carry.basar.service.UserRoleService;
import org.springframework.stereotype.Service;

@Service
public class UserRoleServiceImpl implements UserRoleService {

  private final UserRolRepository userRolRepository;

  public UserRoleServiceImpl(UserRolRepository userRolRepository) {
    this.userRolRepository = userRolRepository;
  }

  @Override
  public void removeAllRolesForUser(Long userId) {
    System.out.println("Iniciando eliminación de roles para el usuario con ID: " + userId);
    this.userRolRepository.findByUserId(userId)
            .doOnSubscribe(sub -> System.out.println("Buscando roles para el usuario con ID: " + userId))
            .doOnNext(userRole -> {
              if (null != userRole)
                System.out.println("Suscritos userRole: " + userRole.getRoleId());
            })
            .doOnError(e -> System.out.println("Error al buscar roles para el usuario: " + e.getMessage()))
            .flatMap(userRol -> userRolRepository.delete(userRol)
                    .doOnSuccess(aVoid -> System.out.println("Rol eliminado: " + userRol.getRoleId()))
                    .doOnError(error -> System.err.println("Error al eliminar rol: " + error.getMessage())))
            .then()
            .subscribe(
                    null,
                    error -> System.err.println("Error al eliminar roles: " + error.getMessage()),
                    () -> System.out.println("Roles eliminados para el usuario con ID: " + userId)
            );
  }
}
