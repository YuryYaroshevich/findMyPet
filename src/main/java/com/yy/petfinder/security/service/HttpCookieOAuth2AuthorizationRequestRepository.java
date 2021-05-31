package com.yy.petfinder.security.service;

import java.util.Base64;
import java.util.Optional;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.server.ServerAuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.SerializationUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class HttpCookieOAuth2AuthorizationRequestRepository
    implements ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> {
  public static final String OAUTH2_AUTH_REQ = "oauth2_auth_request";
  private static final int COOKIE_EXPIRES_SEC = 180;

  @Override
  public Mono<OAuth2AuthorizationRequest> loadAuthorizationRequest(ServerWebExchange exchange) {
    return Optional.ofNullable(exchange.getRequest().getCookies())
        .map(cookies -> cookies.getFirst(OAUTH2_AUTH_REQ))
        .map(HttpCookie::getValue)
        .map(val -> Base64.getUrlDecoder().decode(val))
        .map(SerializationUtils::deserialize)
        .map(OAuth2AuthorizationRequest.class::cast)
        .map(Mono::just)
        .orElseGet(Mono::empty);
  }

  @Override
  public Mono<Void> saveAuthorizationRequest(
      OAuth2AuthorizationRequest authorizationRequest, ServerWebExchange exchange) {
    final String authReqSerialized =
        Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(authorizationRequest));
    final ResponseCookie cookie =
        ResponseCookie.from(OAUTH2_AUTH_REQ, authReqSerialized).maxAge(COOKIE_EXPIRES_SEC).build();
    exchange.getResponse().addCookie(cookie);
    return Mono.just(new Object()).then();
  }

  @Override
  public Mono<OAuth2AuthorizationRequest> removeAuthorizationRequest(ServerWebExchange exchange) {
    Optional.ofNullable(exchange.getResponse().getCookies())
        .map(cookies -> cookies.remove(OAUTH2_AUTH_REQ));
    return loadAuthorizationRequest(exchange);
  }
}
