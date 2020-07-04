package com.yy.petfinder.security.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class JWTHeadersExchangeMatcher implements ServerWebExchangeMatcher {
  @Override
  public Mono<MatchResult> matches(final ServerWebExchange exchange) {
    final ServerHttpRequest req = exchange.getRequest();
    final boolean containsAuthorization = req.getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
    return containsAuthorization ? MatchResult.match() : MatchResult.notMatch();
  }
}
