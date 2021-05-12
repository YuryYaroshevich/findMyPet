package com.yy.petfinder.security.service;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class SpotAdRequestMatcher implements ServerWebExchangeMatcher {
  @Override
  public Mono<MatchResult> matches(ServerWebExchange exchange) {
    final ServerHttpRequest req = exchange.getRequest();
    final boolean petAdsEndpoint = req.getPath().toString().contains("/pets/spotAd");
    return petAdsEndpoint ? MatchResult.match() : MatchResult.notMatch();
  }
}
