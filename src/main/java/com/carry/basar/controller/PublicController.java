package com.carry.basar.controller;

import com.carry.basar.model.dto.auth.AuthRequest;
import com.carry.basar.model.User;
import com.carry.basar.model.dto.auth.AuthResponse;
import com.carry.basar.model.dto.role.RolesListResponse;
import com.carry.basar.model.dto.user.CreateUserRequest;
import com.carry.basar.model.dto.user.ChangePasswordResponse;
import com.carry.basar.service.RoleService;
import com.carry.basar.service.UserService;
import com.carry.basar.service.EmailService;
import com.carry.basar.model.dto.user.ChangePasswordRequest;


import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/public/usr")
public class PublicController {

  private final UserService service;
  private final RoleService roleService;
  private final EmailService emailService;

  private static final Logger log = LoggerFactory.getLogger(PublicController.class);

  public PublicController(UserService service, EmailService emailService, RoleService roleService) {
    this.service = service;
    this.emailService = emailService;
    this.roleService = roleService;
  }

  @PostMapping("/login")
  public Mono<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
    return service.authenticate(authRequest.username(), authRequest.password());
  }

  @GetMapping("/listRolesForSignUpForm")
  public Flux<RolesListResponse> listRolesForSignUp() {
    return roleService.listRolesForSignUp();
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<User> register(@Valid @RequestBody CreateUserRequest user) {
    log.info("CONTROLLER USER: {}", user.username());
    return service.register(user);
  }

  @GetMapping("/check")
  public Mono<ResponseEntity<Void>> checkIfTokenIsValid() {
    return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(auth -> auth != null && auth.isAuthenticated())
            .map(auth -> ResponseEntity.ok().<Void>build())
            .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
  }

  /**
   * Endpoint to bring the password if you forgot it and can't enter
   *
   * @param request : consists of a user email and a new password
   * @return an object with a username and a new password
   */
  @PostMapping("/changePassword")
  public Mono<ChangePasswordResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
    return service.changeUserPassword(request);
  }

  /**
   * Endpoint to test an email send
   *
   * @param to      : address to where you are sending an email
   * @param subject : subject of the email
   * @param text    : message itself
   * @return a message "sent" is success
   */
  @PostMapping("/mail/send")
  public Mono<ResponseEntity<String>> send(@RequestParam String to,
                                           @RequestParam String subject,
                                           @RequestParam String text) {
    return emailService.sendAsync(to, subject, text)
            .thenReturn(ResponseEntity.ok("Sent 🚀"))
            .onErrorResume(e ->
                    Mono.just(ResponseEntity.status(401)
                            .body("Error SMTP: " + e.getMessage())));
  }

}