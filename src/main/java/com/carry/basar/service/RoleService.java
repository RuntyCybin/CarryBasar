package com.carry.basar.service;

import com.carry.basar.model.Role;
import com.carry.basar.model.dto.RoleDto;

import com.carry.basar.model.dto.role.UpdateRoleRequest;
import reactor.core.publisher.Mono;

public interface RoleService {

    Mono<RoleDto> findRoleByName(String name);

    Mono<Role> registerRole(RoleDto role);

    Mono<RoleDto> updateRole(UpdateRoleRequest request);
}
