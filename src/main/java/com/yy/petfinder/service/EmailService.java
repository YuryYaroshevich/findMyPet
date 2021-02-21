package com.yy.petfinder.service;

import com.yy.petfinder.rest.model.PasswordUpdateEmail;
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

  public Mono<Void> sendNewPasswordEmail(
      final PasswordUpdateEmail passwordUpdateEmail, final String userId) {
    return Mono.fromRunnable(
        () -> {
          SimpleMailMessage message = new SimpleMailMessage();
          message.setFrom(appEmail);
          message.setTo(passwordUpdateEmail.getEmail());
          message.setSubject(NEW_PASSWORD_SUBJECT);
          message.setText(
              String.format(
                  "To reset your password click the following link: https://%s?key=blablablarandom&userId=%s",
                  passwordUpdateEmail.getFrontendHost(), userId));
          emailSender.send(message);
        });
  }
}
