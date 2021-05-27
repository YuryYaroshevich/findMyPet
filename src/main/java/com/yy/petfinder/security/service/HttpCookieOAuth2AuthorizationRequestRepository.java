package com.yy.petfinder.security.service;

import java.util.Base64;
import org.springframework.security.oauth2.client.web.server.ServerAuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.SerializationUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class HttpCookieOAuth2AuthorizationRequestRepository
    implements ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> {
  public static final String OAUTH2_AUTH_REQ = "oauth2_auth_request";
  public static final String REDIRECT_URI = "redirect_uri";

  @Override
  public Mono<OAuth2AuthorizationRequest> loadAuthorizationRequest(ServerWebExchange exchange) {
    final String oauthCookieVal =
        exchange.getRequest().getCookies().getFirst(OAUTH2_AUTH_REQ).getValue();
    final OAuth2AuthorizationRequest oAuth2AuthorizationRequest =
        OAuth2AuthorizationRequest.class.cast(
            SerializationUtils.deserialize(Base64.getUrlDecoder().decode(oauthCookieVal)));
    return Mono.just(oAuth2AuthorizationRequest);
  }

  @Override
  public Mono<Void> saveAuthorizationRequest(
      OAuth2AuthorizationRequest authorizationRequest, ServerWebExchange exchange) {
    return null;
  }

  @Override
  public Mono<OAuth2AuthorizationRequest> removeAuthorizationRequest(ServerWebExchange exchange) {
    return loadAuthorizationRequest(exchange);
  }
}
