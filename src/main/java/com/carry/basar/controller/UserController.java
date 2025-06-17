package com.carry.basar.controller;

import com.carry.basar.model.dto.auth.AuthRequest;
import com.carry.basar.model.User;
import com.carry.basar.model.dto.auth.AuthResponse;
import com.carry.basar.model.dto.user.ChangePwdNotLoggedUserRequest;
import com.carry.basar.model.dto.user.CreateUserRequest;
import com.carry.basar.service.EmailService;
import com.carry.basar.service.UserService;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/public/usr")
public class UserController {

  private final UserService service;
  private final EmailService emailService;

  public UserController(UserService service, EmailService emailService) {
    this.service = service;
    this.emailService = emailService;
  }

  @PostMapping("/login")
  public Mono<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
    return service.authenticate(authRequest.getUsername(), authRequest.getPassword());
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<User> register(@Valid @RequestBody CreateUserRequest user) {
    return service.register(user);
  }

  @PostMapping("/changePassword")
  public Mono<ResponseEntity<String>> changePwd(@Valid @RequestBody ChangePwdNotLoggedUserRequest request) {
    return service.changePwd(request)
            .map(ResponseEntity::ok)
            .onErrorResume(err -> Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + err.getMessage())));
  }

  @GetMapping("/check")
  public Mono<ResponseEntity<Void>> checkIfTokenIsValid() {
    return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(auth -> auth != null && auth.isAuthenticated())
            .map(auth -> ResponseEntity.ok().<Void>build())
            .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
  }

  @PostMapping("/mail/send")
  public Mono<ResponseEntity<String>> send(@RequestParam String to, @RequestParam String subject, @RequestParam String text) {
    System.out.println("LLEGO AL CONTROLLER");
    return emailService.sendAsync(to, subject, text)
            .thenReturn(ResponseEntity.ok("Enviado 🚀"))
            .onErrorResume(e ->
                    Mono.just(ResponseEntity.status(401)
                            .body("Error SMTP: " + e.getMessage())));
  }

}