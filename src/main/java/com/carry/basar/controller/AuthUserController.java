package com.carry.basar.controller;

import javax.validation.Valid;

import com.carry.basar.model.User;
import org.springframework.data.relational.core.query.Update;
import org.springframework.web.bind.annotation.*;

import com.carry.basar.model.dto.user.UpdateUserRequest;
import com.carry.basar.service.UserService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/api/user")
public class AuthUserController {

  private final UserService service;

  public AuthUserController(UserService service) {
    this.service = service;
  }

  @PutMapping("/update")
  public Mono<User> update(@Valid @RequestBody UpdateUserRequest userRequest) {
    return service.update(userRequest);
  }

}
