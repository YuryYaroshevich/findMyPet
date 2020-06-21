package com.yy.petfinder.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public final class SecurityUtils {

  private SecurityUtils() {}

  public static Mono<String> getUserFromRequest(ServerWebExchange serverWebExchange) {
    return serverWebExchange
        .getPrincipal()
        .cast(UsernamePasswordAuthenticationToken.class)
        .map(UsernamePasswordAuthenticationToken::getPrincipal)
        .cast(User.class)
        .map(User::getUsername);
  }
}
