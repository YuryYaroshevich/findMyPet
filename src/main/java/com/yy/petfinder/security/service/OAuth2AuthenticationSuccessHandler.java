package com.yy.petfinder.security.service;

import com.yy.petfinder.model.User;
import com.yy.petfinder.model.UserRandomKey;
import com.yy.petfinder.persistence.UserRandomKeyRepository;
import com.yy.petfinder.persistence.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
public class OAuth2AuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {
  private final String allowedRedirectUri;
  private final UserRandomKeyRepository userRandomKeyRepository;
  private final UserRepository userRepository;

  public OAuth2AuthenticationSuccessHandler(
      @Value("${oauth2.authorizedRedirectUri}") final String allowedRedirectUri,
      final UserRandomKeyRepository userRandomKeyRepository,
      final UserRepository userRepository) {
    this.allowedRedirectUri = allowedRedirectUri;
    this.userRandomKeyRepository = userRandomKeyRepository;
    this.userRepository = userRepository;
  }

  @Override
  public Mono<Void> onAuthenticationSuccess(
      WebFilterExchange webFilterExchange, Authentication authentication) {
    final ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
    final DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
    final String email = (String) oAuth2User.getAttributes().get("email");
    return userRepository
        .findByEmail(email)
        .map(User::getId)
        .map(
            userId ->
                UserRandomKey.builder()
                    .id(userId)
                    .randomKey(UUID.randomUUID().toString())
                    .createdAt(Instant.now())
                    .build())
        .flatMap(userRandomKeyRepository::save)
        .doOnNext(
            userRandomKey -> {
              final UriComponents uriComponents =
                  UriComponentsBuilder.fromUriString(allowedRedirectUri)
                      .queryParam("id", userRandomKey.getId())
                      .queryParam("key", userRandomKey.getRandomKey())
                      .build();
              response.setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
              response.getHeaders().add("Location", uriComponents.toUriString());
            })
        .switchIfEmpty(Mono.error(new BadCredentialsException("user wasn't found")))
        .then();
  }
}
