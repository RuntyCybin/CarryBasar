package com.carry.basar.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
public class IndexController {
  @GetMapping("/")
  public Mono<Void> redirectToLogin(ServerWebExchange serverWebExchange) {
    return Mono.fromRunnable(() -> {
      serverWebExchange.getResponse()
              .setStatusCode(HttpStatus.PERMANENT_REDIRECT);
      serverWebExchange.getResponse()
              .getHeaders()
              .setLocation(serverWebExchange.getRequest().getURI().resolve("/public/login.html"));
    });
  }
}
