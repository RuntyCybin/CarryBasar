package com.carry.basar.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.carry.basar.service.EmailService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class EmailServiceImpl implements EmailService {

  private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

  private final JavaMailSenderImpl mailSender;
  private final Scheduler mailScheduler;

  @Value("${spring.mail.username}")
  private String from;

  public EmailServiceImpl(JavaMailSenderImpl javaMailSender) {
    // 1️⃣ Un pool de virtual-threads que NO se cierra tras cada envío
    ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    this.mailSender = javaMailSender;
    this.mailScheduler = Schedulers.fromExecutor(executorService);
  }

  @PostConstruct
  void verifySmtp() {
    try {
      mailSender.testConnection();
      log.info("Conexión SMTP OK");
    } catch (MessagingException e) {
      log.warn("No se pudo verificar la conexión SMTP: {}", e.getMessage());
    }
  }

  /** Sincrónico */
  @Override
  public Mono<Void> send(String to, String subject, String text) {
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setFrom(from);
    msg.setTo(to);
    msg.setSubject(subject);
    msg.setText(text);
    mailSender.send(msg);

    return Mono.empty();
  }

  /** Asincrónico */
  @Override
  public Mono<Void> sendAsync(String to, String subject, String body) {
    return Mono.fromRunnable(() -> sendBlocking(to, subject, body))
            .subscribeOn(mailScheduler).then();   // virtual threads
  }

  private void sendBlocking(String to, String subject, String text) {
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setFrom(from);
    msg.setTo(to);
    msg.setSubject(subject);
    msg.setText(text);
    mailSender.send(msg);
  }
}
