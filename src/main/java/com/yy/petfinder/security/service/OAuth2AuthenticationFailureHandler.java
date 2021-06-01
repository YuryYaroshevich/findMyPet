package com.yy.petfinder.security.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import reactor.core.publisher.Mono;

public class OAuth2AuthenticationFailureHandler implements ServerAuthenticationFailureHandler {
  private final String allowedRedirectUri;

  public OAuth2AuthenticationFailureHandler(
      @Value("${oauth2.authorizedRedirectUris}") final String allowedRedirectUri) {
    this.allowedRedirectUri = allowedRedirectUri;
  }

  @Override
  public Mono<Void> onAuthenticationFailure(
      WebFilterExchange webFilterExchange, AuthenticationException exception) {
    final ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
    response.setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
    response.getHeaders().add("Location", allowedRedirectUri + "?error=failed_to_authorize");
    return Mono.just(new Object()).then();
  }
}
