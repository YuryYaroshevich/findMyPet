package com.yy.petfinder.service;

import com.yy.petfinder.rest.model.EmailMessageData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EmailService {
  private final JavaMailSender emailSender;
  private final String appEmail;

  @Autowired
  public EmailService(
      final JavaMailSender emailSender, @Value("${spring.mail.username}") final String appEmail) {
    this.emailSender = emailSender;
    this.appEmail = appEmail;
  }

  public Mono<Void> sendSpotAdEmail(final String email, final EmailMessageData emailMessageData) {
    return Mono.fromRunnable(
        () -> {
          final SimpleMailMessage message = new SimpleMailMessage();
          message.setFrom(appEmail);
          message.setTo(email);
          message.setSubject(emailMessageData.getSubject());

          message.setText(emailMessageData.getText());
          emailSender.send(message);
        });
  }
}
