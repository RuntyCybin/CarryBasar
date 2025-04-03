package com.carry.basar.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class Utils {

  public Mono<String> getAuthenticatedUsername() {
    return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> {
              Authentication authentication = ctx.getAuthentication();
              if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName(); // Devuelve el username extraído del
                // token
              } else {
                System.out.println("User was not authenticated");
              }
              return null;
            })
            .switchIfEmpty(Mono.error(new RuntimeException("Authentication failed")));
  }
}
