package com.carry.basar.controller;

import com.carry.basar.utils.Utils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/api")
public class SecureController {

  private final Utils utils;

  public SecureController(Utils utils) {
    this.utils = utils;
  }

  // requires JWT authentication.
  @GetMapping("/test")
  public Mono<String> test() {
    return ReactiveSecurityContextHolder.getContext()
            .flatMap(ctx -> utils.getAuthenticatedUser(Mono.just(ctx)))
            .map(authentication -> "Login successful " + authentication);
  }

  @GetMapping("/me")
  public Mono<String> getAuthenticatedUser() {
    return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> {
              Authentication authentication = ctx.getAuthentication();
              if (authentication != null && authentication.isAuthenticated()) {
                return "Contexto de seguridad configurado correctamente. Usuario autenticado: " + authentication.getName();
              } else {
                return "Contexto de seguridad configurado, pero no hay usuario autenticado.";
              }
            })
            .defaultIfEmpty("Contexto de seguridad no configurado.");
  }
}
