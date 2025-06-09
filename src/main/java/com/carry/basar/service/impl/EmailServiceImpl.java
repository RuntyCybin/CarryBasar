package com.carry.basar.service.impl;

import com.carry.basar.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.Executors;

@Service
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender javaMailSender;

  @Value("${spring.mail.username}")
  private String from;

  public EmailServiceImpl(JavaMailSender javaMailSender) {
    this.javaMailSender = javaMailSender;
  }

  /** Sincrónico */
  @Override
  public Mono<Void> send(String to, String subject, String text) {
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setFrom(from);
    msg.setTo(to);
    msg.setSubject(subject);
    msg.setText(text);
    javaMailSender.send(msg);

    return Mono.empty();
  }

  /** Asincrónico */
  @Override
  public Mono<Void> sendAsync(String to, String subject, String text) {
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      executor.submit(() -> send(to, subject, text));
    }

    return Mono.empty();
  }
}
