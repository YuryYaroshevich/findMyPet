package com.yy.petfinder.service;

import com.yy.petfinder.model.UserRandomKey;
import com.yy.petfinder.rest.model.EmailMessageData;
import com.yy.petfinder.rest.model.PasswordUpdateEmail;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PasswdUpdateEmailService {
  private static final String NEW_PASSWORD_SUBJECT = "Password reset";
  private static final String LINK_PLACEHOLDER = "{link}";

  private final JavaMailSender emailSender;
  private final String appEmail;
  private final EmailService emailService;

  @Autowired
  public PasswdUpdateEmailService(
      final JavaMailSender emailSender,
      @Value("${spring.mail.username}") final String appEmail,
      final EmailService emailService) {
    this.emailSender = emailSender;
    this.appEmail = appEmail;
    this.emailService = emailService;
  }

  public Mono<UserRandomKey> sendNewPasswordEmail(
      final PasswordUpdateEmail passwordUpdateEmail, final String userId) {
    final String randomKey = UUID.randomUUID().toString();
    final String emailText = emailText(passwordUpdateEmail, userId, randomKey);
    final EmailMessageData emailMessageData =
        passwordUpdateEmail.getEmailMessageData().toBuilder().text(emailText).build();
    return emailService
        .sendSpotAdEmail(passwordUpdateEmail.getEmail(), emailMessageData)
        .thenReturn(
            UserRandomKey.builder()
                .id(userId)
                .randomKey(randomKey)
                .createdAt(Instant.now())
                .build());
  }

  private String emailText(
      final PasswordUpdateEmail passwordUpdateEmail, final String userId, final String randomKey) {
    final String link =
        String.format(
            "%s?key=%s&userId=%s", passwordUpdateEmail.getFrontendHost(), randomKey, userId);
    final String emailText =
        passwordUpdateEmail.getEmailMessageData().getText().replace(LINK_PLACEHOLDER, link);
    return emailText;
  }
}
