package com.carry.basar.service.impl;

import com.carry.basar.model.Role;
import com.carry.basar.model.dto.RoleDto;
import com.carry.basar.model.dto.role.RolesListResponse;
import com.carry.basar.model.dto.role.UpdateRoleRequest;
import com.carry.basar.model.repository.RoleRepository;
import com.carry.basar.model.repository.UserRepository;
import com.carry.basar.model.repository.UserRolRepository;
import com.carry.basar.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RoleServiceImpl implements RoleService {

  private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

  private final RoleRepository roleRepository;

  private final UserRepository userRepository;

  private final UserRolRepository userRolRepository;

  public RoleServiceImpl(RoleRepository roleRepository, UserRepository userRepository, UserRolRepository userRolRepository) {
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
    this.userRolRepository = userRolRepository;
  }

  @Override
  public Mono<RoleDto> findRoleByName(String name) {
    return roleRepository.findByName(name)
            .map(role -> new RoleDto(role.getName()))
            .onErrorResume(e -> {
              log.error("Error searching for a role: {}", e.getMessage());
              return Mono.error(new RuntimeException("Error searching for a role: ", e));
            });
  }

  @Override
  public Mono<Role> registerRole(RoleDto roleDto) {
    return roleRepository.findByName(roleDto.name())
            .flatMap(role -> Mono.<Role>error(new RuntimeException("Role already exists")))
            .switchIfEmpty(Mono.defer(() -> {
              Role newRole = new Role();
              newRole.setName(roleDto.name());
              return roleRepository.save(newRole);
            }))
            .onErrorResume(e -> {
              log.error("Error saving a role: {}", e.getMessage());
              return Mono.error(new RuntimeException("Error saving a role: ", e));
            });
  }

  @Override
  public Mono<RoleDto> updateRole(UpdateRoleRequest request) {
    return roleRepository.findByName(request.getSearchName())
            .flatMap(roleFound -> {
              roleFound.setName(request.getNewName());
              return roleRepository.save(roleFound)
                      .map(savedRole -> new RoleDto(savedRole.getName()));
            });
  }

  @Override
  public Mono<String> deleteRole(String roleNameToErase, String username) {
    // compruebo que el usuario existe
    return userRepository.findByName(username)
            .flatMap(user -> {
              // recogo los roles del usuario
              return this.userRolRepository.findByUserId(user.getId())
                      .switchIfEmpty(Mono.error(new ResponseStatusException(
                              HttpStatus.NOT_FOUND, "Error, no roles for that user")))
                      .flatMap(userRol -> {
                        // recogemos el nombre de cada rol del usuario
                        return this.roleRepository.findById(userRol.getRoleId())
                                .switchIfEmpty(Mono.error(new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "Error, role not found")))
                                .flatMap(role -> {
                                  // comprobamos que el usuario es administrador
                                  if (role.getName().equals("ADMIN")) {
                                    return this.roleRepository.findByName(roleNameToErase)
                                            .switchIfEmpty(Mono.error(new ResponseStatusException(
                                                    HttpStatus.NOT_FOUND, "Error, role not found")))
                                            .flatMap(this.roleRepository::delete).then(Mono.just("Role deleted successfully"));
                                  }
                                  return Mono.empty();
                                });
                      })
                      .next()
                      .switchIfEmpty(Mono.just("User is not an administrator"));
            })
            .onErrorResume(ResponseStatusException.class, Mono::error)
            .onErrorResume(e -> {
              return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting role: " + e.getMessage()));
            });
  }

  @Override
  public Flux<RolesListResponse> listAllRoles(String username) {
    // compruebo que el usuario existe
    return userRepository.findByName(username)
            .flatMapMany(user -> {
              return this.roleRepository
                      .findAll()
                      .map(role -> new RolesListResponse(role.getName()));
            });
  }
}
