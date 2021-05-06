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
  private final EmailService emailService;

  @Autowired
  public NotificationService(final UserService userService, final EmailService emailService) {
    this.userService = userService;
    this.emailService = emailService;
  }

  public void notifyUsers(final Flux<String> userIds, final EmailMessageData emailMessageData) {
    userIds
        .flatMap(ownerId -> userService.getUser(ownerId))
        .map(PrivateUserView::getEmail)
        .flatMap(email -> emailService.sendSpotAdEmail(email, emailMessageData))
        .subscribeOn(Schedulers.parallel())
        .subscribe();
  }
}
