package com.yy.petfinder.util;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

public final class UserIdRetriever {
  public static Mono<String> userIdFromContext() {
    return ReactiveSecurityContextHolder.getContext()
        .map(sc -> sc.getAuthentication().getPrincipal())
        .cast(UserDetails.class)
        .map(UserDetails::getUsername);
  }
}
