package com.carry.basar.controller;

import javax.validation.Valid;

import com.carry.basar.model.User;
import com.carry.basar.model.dto.user.CreateUserRequest;
import com.carry.basar.model.dto.user.UpdateUserResponse;
import com.carry.basar.utils.Utils;
import org.springframework.data.relational.core.query.Update;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.carry.basar.model.dto.user.UpdateUserRequest;
import com.carry.basar.service.UserService;

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

}
