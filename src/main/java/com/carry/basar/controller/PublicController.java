package com.carry.basar.controller;

import com.carry.basar.model.dto.auth.AuthRequest;
import com.carry.basar.model.User;
import com.carry.basar.model.dto.auth.AuthResponse;
import com.carry.basar.model.dto.role.RolesListResponse;
import com.carry.basar.model.dto.user.CreateUserRequest;
import com.carry.basar.model.dto.user.RecoverPwdRequest;
import com.carry.basar.model.dto.user.RecoverPwdResponse;
import com.carry.basar.service.UserService;
import com.carry.basar.service.EmailService;
import com.carry.basar.model.dto.user.ChangePwdNotLoggedUserRequest;


import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/public/usr")
public class PublicController {

  private final UserService service;
  private final EmailService emailService;

  public PublicController(UserService service, EmailService emailService) {
    this.service = service;
    this.emailService = emailService;
  }

  @PostMapping("/login")
  public Mono<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
    return service.authenticate(authRequest.getUsername(), authRequest.getPassword());
  }

    @GetMapping("/getPublicRoles")
    public Flux<RolesListResponse> listPublicRoles() {
        return service.listPublicRoles();
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<User> register(@Valid @RequestBody CreateUserRequest user) {
        System.out.println("CONTROLLER USER: " + user.getUsername());
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

    @PostMapping("/recoverpwd")
    public Mono<RecoverPwdResponse> changePassword(@Valid @RequestBody RecoverPwdRequest request) {
        return service.changeUserPassword(request);
    }

    @PostMapping("/changePassword")
    public Mono<ResponseEntity<String>> changePwd(@Valid @RequestBody ChangePwdNotLoggedUserRequest request) {
        return service.changePwd(request)
                .map(ResponseEntity::ok)
                .onErrorResume(err -> Mono.just(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Error: " + err.getMessage())));
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