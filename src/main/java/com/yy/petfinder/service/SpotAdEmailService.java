package com.yy.petfinder.service;

import com.yy.petfinder.rest.model.SpotAdView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SpotAdEmailService {
  private static final String SPOT_AD_SUBJECT = "Someone's seen this pet. Is it yours?";

  private final JavaMailSender emailSender;
  private final String appEmail;

  @Autowired
  public SpotAdEmailService(
      final JavaMailSender emailSender, @Value("${spring.mail.username}") final String appEmail) {
    this.emailSender = emailSender;
    this.appEmail = appEmail;
  }

  public Mono<Void> sendSpotAdEmail(final String email, final SpotAdView spotAdView) {
    return Mono.fromRunnable(
        () -> {
          final SimpleMailMessage message = new SimpleMailMessage();
          message.setFrom(appEmail);
          message.setTo("");
          message.setSubject(SPOT_AD_SUBJECT);

          message.setText(spotAdView.getEmailText());
          emailSender.send(message);
        });
  }
}
