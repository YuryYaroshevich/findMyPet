package com.yy.petfinder.security.service;

import com.yy.petfinder.model.User;
import com.yy.petfinder.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class OAuth2AuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {
  private final String allowedRedirectUri;
  private final TokenService tokenService;
  private final UserRepository userRepository;

  public OAuth2AuthenticationSuccessHandler(
      @Value("${oauth2.authorizedRedirectUri}") final String allowedRedirectUri,
      final TokenService tokenService,
      final UserRepository userRepository) {
    this.allowedRedirectUri = allowedRedirectUri;
    this.tokenService = tokenService;
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
        .map(tokenService::createToken)
        .doOnNext(
            token -> {
              response.setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
              response.getHeaders().add("Location", allowedRedirectUri + "?token=" + token);
            })
        .switchIfEmpty(Mono.error(new BadCredentialsException("user wasn't found")))
        .then();
  }
}
