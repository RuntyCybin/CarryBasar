package com.carry.basar.controller;

import com.carry.basar.model.dto.auth.AuthRequest;
import com.carry.basar.model.User;
import com.carry.basar.model.dto.user.CreateUserRequest;
import com.carry.basar.service.UserService;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/public/usr")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public Mono<String> secureEndpoint(@Valid @RequestBody AuthRequest authRequest) {
        return service.authenticate(authRequest.getUsername(), authRequest.getPassword())
                .onErrorResume(e ->
                        Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<User> register(@Valid @RequestBody CreateUserRequest user) {
        return service.register(user);
    }
    
}