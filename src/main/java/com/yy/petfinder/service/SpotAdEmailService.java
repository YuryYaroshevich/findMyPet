package com.yy.petfinder.service;

import com.yy.petfinder.rest.model.EmailMessageData;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SpotAdEmailService {
  private static final String ID_PLACEHOLDER = "{spotAdId}";

  private final EmailService emailService;

  public SpotAdEmailService(final EmailService emailService) {
    this.emailService = emailService;
  }

  public Mono<Void> notifyAboutSpotAd(
      final String email, final String spotAdId, final EmailMessageData emailMessageData) {
    final String textWithId = emailMessageData.getText().replace(ID_PLACEHOLDER, spotAdId);
    final EmailMessageData messageData = emailMessageData.toBuilder().text(textWithId).build();
    return emailService.sendEmail(email, messageData);
  }
}
