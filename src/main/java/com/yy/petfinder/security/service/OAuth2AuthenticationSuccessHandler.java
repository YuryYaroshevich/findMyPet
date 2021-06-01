package com.yy.petfinder.security.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class OAuth2AuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {
  private final String allowedRedirectUri;
  private final TokenService tokenService;

  public OAuth2AuthenticationSuccessHandler(
      @Value("${oauth2.authorizedRedirectUris}") final String allowedRedirectUri,
      final TokenService tokenService) {

    this.allowedRedirectUri = allowedRedirectUri;
    this.tokenService = tokenService;
  }

  @Override
  public Mono<Void> onAuthenticationSuccess(
      WebFilterExchange webFilterExchange, Authentication authentication) {
    final ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
    response.setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
    final String token = tokenService.createToken(authentication.getPrincipal().toString());
    response.getHeaders().add("Location", allowedRedirectUri + "?token=" + token);
    return Mono.just(new Object()).then();
  }
}
