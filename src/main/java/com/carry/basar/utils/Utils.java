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

    if (createUserRequest.getUsername().matches("\\d+")) {
      return false;
    }

    if ((createUserRequest.getUsername() == null || createUserRequest.getUsername().isEmpty()) && (createUserRequest.getUsername().matches("\\d+"))) {
      return false;
    }
    if (createUserRequest.getPassword() == null || createUserRequest.getPassword().isEmpty()) {
      return false;
    }
    if (createUserRequest.getEmail() == null || createUserRequest.getEmail().isEmpty()) {
      return false;
    }
    if (createUserRequest.getRoles() == null || createUserRequest.getRoles().isEmpty()) {
      return false;
    }

    return true;
  }

  public User mapUser(CreateUserRequest createUserRequest) {
    User user = new User();
    user.setName(createUserRequest.getUsername());
    user.setEmail(createUserRequest.getEmail());
    user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
    return user;
  }
}
