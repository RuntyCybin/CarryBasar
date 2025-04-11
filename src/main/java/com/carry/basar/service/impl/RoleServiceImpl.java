package com.carry.basar.service.impl;

import com.carry.basar.model.Role;
import com.carry.basar.model.dto.RoleDto;
import com.carry.basar.model.repository.RoleRepository;
import com.carry.basar.service.RoleService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RoleServiceImpl implements RoleService {

  private final RoleRepository roleRepository;

  public RoleServiceImpl(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  @Override
  public Mono<RoleDto> findRoleByName(String name) {
    return roleRepository.findByName(name)
            .map(role -> {
              RoleDto roleDto = new RoleDto();
              roleDto.setName(role.getName());
              return roleDto;
            })
            .onErrorResume(e -> {
              System.out.println("Error searching for a role: " + e.getMessage());
              return Mono.error(new RuntimeException("Error searching for a role: ", e));
            });
  }

  @Override
  public Mono<Role> registerRole(RoleDto roleDto) {
    return roleRepository.findByName(roleDto.getName())
            .flatMap(role -> Mono.<Role>error(new RuntimeException("Role already exists")))
            .switchIfEmpty(Mono.defer(() -> {
              Role newRole = new Role();
              newRole.setName(roleDto.getName());
              return roleRepository.save(newRole);
            }))
            .onErrorResume(e -> {
              System.out.println("Error saving a role: " + e.getMessage());
              return Mono.error(new RuntimeException("Error saving a role: ", e));
            });
  }
}
