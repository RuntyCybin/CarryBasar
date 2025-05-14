package com.carry.basar.service;

import com.carry.basar.model.User;
import com.carry.basar.model.dto.auth.AuthResponse;
import com.carry.basar.model.dto.user.CreateUserRequest;
import com.carry.basar.model.dto.user.ListUsersResponse;
import com.carry.basar.model.dto.user.UpdateUserRequest;
import com.carry.basar.model.dto.user.UpdateUserResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface UserService {

  Mono<AuthResponse> authenticate(String username, String password);

  Mono<User> register(CreateUserRequest createUserRequest);

  Mono<UpdateUserResponse> update(UpdateUserRequest updateUserRequest);

  Mono<Void> removeAllRoles(String username);

  Mono<String> removeUser(String username);

  Flux<ListUsersResponse> listAllUsers();

}
