package com.yy.petfinder.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class NotificationService {
  public Flux notifyUsers(final Flux ownerIds) {
    return null;
  }
}
