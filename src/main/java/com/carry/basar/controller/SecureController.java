package com.carry.basar.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/api")
public class SecureController {


    // requires JWT authentication.
    @GetMapping("/test")
    public Mono<String> test() {
        return Mono.just("Login successful");
    }
}
