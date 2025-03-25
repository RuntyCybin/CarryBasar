package com.carry.basar.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
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

    @GetMapping("/me")
    public Mono<String> getAuthenticatedUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> {
                    Authentication authentication = ctx.getAuthentication();
                    return authentication != null && authentication.isAuthenticated()
                            ? "Usuario autenticado: " + authentication.getName()
                            : "No hay usuario autenticado";
                });
    }
}
