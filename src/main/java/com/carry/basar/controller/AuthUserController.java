package com.carry.basar.controller;

import javax.validation.Valid;

import com.carry.basar.model.dto.role.RolesListResponse;
import com.carry.basar.model.dto.user.ListUsersResponse;
import com.carry.basar.model.dto.user.UpdateUserResponse;
import com.carry.basar.utils.Utils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.carry.basar.model.dto.user.UpdateUserRequest;
import com.carry.basar.service.UserService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/api/user")
public class AuthUserController {

  private final UserService service;

  private final Utils utils;

  public AuthUserController(UserService service, Utils utils) {
    this.service = service;
    this.utils = utils;
  }

  @PutMapping("/update")
  public Mono<UpdateUserResponse> update(@Valid @RequestBody UpdateUserRequest userRequest) {
    return service.update(userRequest);
  }

  @PostMapping("/removeRoles")
  public Mono<Void> removeRoles() {
    return ReactiveSecurityContextHolder.getContext()
            .flatMap(ctx -> utils.getAuthenticatedUser(Mono.just(ctx)))
            .map(authentication -> authentication)
            .flatMap(service::removeAllRoles);
  }

  @DeleteMapping("/delete")
  public Mono<String> delete() {
    return ReactiveSecurityContextHolder.getContext()
            .flatMap(ctx -> utils.getAuthenticatedUser(Mono.just(ctx)))
            .map(authentication -> authentication)
            .flatMap(service::removeUser);
  }

  @GetMapping("/list")
  public Flux<ListUsersResponse> listAllUsers() {
    return ReactiveSecurityContextHolder.getContext()
            .flatMap(ctx -> utils.getAuthenticatedUser(Mono.just(ctx)))
            .flatMapMany(auth -> service.listAllUsers());
  }

  @GetMapping("/listRoles")
  public Flux<RolesListResponse> listUserRoles() {
    return ReactiveSecurityContextHolder.getContext()
            .flatMap(ctx -> utils.getAuthenticatedUser(Mono.just(ctx)))
            .map(authentication -> authentication)
            .flatMapMany(service::listAllRolesByUserName);
  }

}
