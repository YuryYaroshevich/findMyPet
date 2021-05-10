package com.yy.petfinder.service;

import com.yy.petfinder.rest.model.EmailMessageData;
import com.yy.petfinder.rest.model.PrivateUserView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Service
public class NotificationService {
  private final UserService userService;
  private final SpotAdEmailService spotAdEmailService;

  @Autowired
  public NotificationService(
      final UserService userService, final SpotAdEmailService spotAdEmailService) {
    this.userService = userService;
    this.spotAdEmailService = spotAdEmailService;
  }

  public void notifyUsers(
      final Flux<String> userIds, final String spotAdId, final EmailMessageData emailMessageData) {
    userIds
        .flatMap(ownerId -> userService.getUser(ownerId))
        .map(PrivateUserView::getEmail)
        .flatMap(email -> spotAdEmailService.notifyAboutSpotAd(email, spotAdId, emailMessageData))
        .subscribeOn(Schedulers.parallel())
        .subscribe();
  }
}
