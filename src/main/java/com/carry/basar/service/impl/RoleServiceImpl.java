package com.carry.basar.service.impl;

import com.carry.basar.model.Role;
import com.carry.basar.model.RoleDto;
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
        Role role = new Role();
        role.setName(roleDto.getName());
        return roleRepository.save(role).onErrorResume(e -> {
            System.out.println("Error saving a role: " + e.getMessage());
            return Mono.error(new RuntimeException("Error saving a role: ", e));
        });
    }
}
