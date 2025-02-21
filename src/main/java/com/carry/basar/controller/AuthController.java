package com.carry.basar.controller;

import com.carry.basar.config.JwtUtil;
import com.carry.basar.model.AuthRequest;
import com.carry.basar.model.User;
import com.carry.basar.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/public/usr")
public class AuthController {

    @Autowired
    private AuthService service;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Mono<String> secureEndpoint(@RequestBody AuthRequest authRequest) {
        return service.authenticate(authRequest.getUsername(), authRequest.getPassword())
                .onErrorResume(e -> Mono.error(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid credentials")));
    }

    @PostMapping("/register")
    public Mono<User> register(@RequestBody User user) {
        return service.register(user)
                .onErrorResume(e -> Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Username already exists")));
    }
}