package com.carry.basar.utils;

import com.carry.basar.model.User;
import com.carry.basar.model.dto.user.CreateUserRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class Utils {

  private final PasswordEncoder passwordEncoder;

  public Utils(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

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

  public Mono<String> getAuthenticatedUser(Mono<SecurityContext> ctx) {
    return ctx.map(context -> {
      Authentication authentication = context.getAuthentication();
      return authentication != null && authentication.isAuthenticated()
              ? authentication.getName()
              : "No hay usuario autenticado";
    });

  }


  public boolean isVerified(CreateUserRequest createUserRequest) {

    if (createUserRequest.username().matches("\\d+")) {
      return false;
    }

    if ((createUserRequest.username() == null || createUserRequest.username().isEmpty()) && (createUserRequest.username().matches("\\d+"))) {
      return false;
    }
    if (createUserRequest.password() == null || createUserRequest.password().isEmpty()) {
      return false;
    }
    if (createUserRequest.email() == null || createUserRequest.email().isEmpty()) {
      return false;
    }
    if (createUserRequest.roles() == null || createUserRequest.roles().isEmpty()) {
      return false;
    }

    return true;
  }

  public User mapUser(CreateUserRequest createUserRequest) {
    User user = new User();
    user.setName(createUserRequest.username());
    user.setEmail(createUserRequest.email());
    user.setPassword(passwordEncoder.encode(createUserRequest.password()));
    return user;
  }
}
