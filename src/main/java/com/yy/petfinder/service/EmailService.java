package com.yy.petfinder.service;

import com.yy.petfinder.model.UserRandomKey;
import com.yy.petfinder.rest.model.PasswordUpdateEmail;
import java.time.Instant;
import java.util.UUID;
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

  private static final String NEW_PASSWORD_SUBJECT = "Password reset";

  @Autowired
  public EmailService(
      final JavaMailSender emailSender, @Value("${spring.mail.username}") final String appEmail) {
    this.emailSender = emailSender;
    this.appEmail = appEmail;
  }

  public Mono<UserRandomKey> sendNewPasswordEmail(
      final PasswordUpdateEmail passwordUpdateEmail, final String userId) {

    return Mono.fromCallable(
            () -> {
              final SimpleMailMessage message = new SimpleMailMessage();
              final String randomKey = UUID.randomUUID().toString();
              message.setFrom(appEmail);
              message.setTo(passwordUpdateEmail.getEmail());
              message.setSubject(NEW_PASSWORD_SUBJECT);
              message.setText(
                  String.format(
                      "To reset your password click the following link: %s?key=%s&userId=%s",
                      passwordUpdateEmail.getFrontendHost(), randomKey, userId));
              emailSender.send(message);
              return randomKey;
            })
        .map(
            randomKey ->
                UserRandomKey.builder()
                    .id(userId)
                    .randomKey(randomKey)
                    .createdAt(Instant.now())
                    .build());
  }
}
