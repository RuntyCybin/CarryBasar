package com.carry.basar.service;

import com.carry.basar.model.User;
import com.carry.basar.config.JwtUtil;
import com.carry.basar.model.UserRol;
import com.carry.basar.model.dto.auth.AuthResponse;
import com.carry.basar.model.dto.user.CreateUserRequest;
import com.carry.basar.model.dto.user.UpdateUserRequest;
import com.carry.basar.model.dto.user.UpdateUserResponse;
import com.carry.basar.model.repository.RoleRepository;
import com.carry.basar.model.repository.UserRepository;
import com.carry.basar.model.repository.UserRolRepository;

import java.util.Set;

import com.carry.basar.utils.Utils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface UserService {

  Mono<AuthResponse> authenticate(String username, String password);

  Mono<User> register(CreateUserRequest createUserRequest);

  Mono<UpdateUserResponse> update(UpdateUserRequest updateUserRequest);

  Mono<Void> removeAllRoles(String username);

  Mono<String> removeUser(String username);

}
