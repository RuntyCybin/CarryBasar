package com.carry.basar.service;

import reactor.core.publisher.Mono;

public interface EmailService {
  Mono<Void> send(String to, String subject, String text);
  Mono<Void> sendAsync(String to, String subject, String text);
}
