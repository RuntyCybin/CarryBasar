package com.carry.basar.controller;

import com.carry.basar.model.Role;
import com.carry.basar.model.dto.RoleDto;
import com.carry.basar.model.dto.role.UpdateRoleRequest;
import com.carry.basar.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/api/role")
public class RoleController {

    private final RoleService service;

    public RoleController(RoleService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public Mono<Role> register(@RequestBody RoleDto role) {
        return service.registerRole(role)
                .onErrorResume(e -> Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Role already exists")));
    }

    @GetMapping("/find/{name}")
    public Mono<RoleDto> findRoleByName(@PathVariable String name) {
        return service.findRoleByName(name)
                .onErrorResume(e -> Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Role not found")));
    }

    @PutMapping("/update")
    public Mono<RoleDto> updateRole(@RequestBody UpdateRoleRequest request) {
        return service.updateRole(request)
                .onErrorResume(e -> Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Role not found")));
    }
}
